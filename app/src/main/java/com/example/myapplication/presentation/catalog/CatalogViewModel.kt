package com.example.myapplication.presentation.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.model.AppUser
import com.example.myapplication.domain.model.MovementType
import com.example.myapplication.domain.model.Product
import com.example.myapplication.domain.model.ProductCategory
import com.example.myapplication.domain.model.StockMovement
import com.example.myapplication.domain.model.UserRole
import com.example.myapplication.domain.usecase.AddProductUseCase
import com.example.myapplication.domain.usecase.AddStockMovementUseCase
import com.example.myapplication.domain.usecase.FilterProductsByCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductByNameAndCategoryUseCase
import com.example.myapplication.domain.usecase.GetProductsUseCase
import com.example.myapplication.domain.usecase.SearchProductsUseCase
import com.example.myapplication.domain.usecase.UpdateProductUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CatalogViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val filterProductsByCategoryUseCase: FilterProductsByCategoryUseCase,
    private val addProductUseCase: AddProductUseCase,
    private val getProductByNameAndCategoryUseCase: GetProductByNameAndCategoryUseCase,
    private val updateProductUseCase: UpdateProductUseCase,
    private val addStockMovementUseCase: AddStockMovementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _addProductDraft = MutableStateFlow(restoreDraft())
    val addProductDraft: StateFlow<AddProductDraft> = _addProductDraft.asStateFlow()

    private val _isAddProductDialogVisible = MutableStateFlow(
        savedStateHandle.get<Boolean>(KEY_DIALOG_VISIBLE) ?: false
    )
    val isAddProductDialogVisible: StateFlow<Boolean> = _isAddProductDialogVisible.asStateFlow()

    private val _catalogProducts = MutableStateFlow<List<Product>>(emptyList())
    val catalogProducts: StateFlow<List<Product>> = _catalogProducts.asStateFlow()

    init { loadProducts() }

    fun openAddProductDialog() {
        _isAddProductDialogVisible.value = true
        savedStateHandle[KEY_DIALOG_VISIBLE] = true
    }

    fun openAddProductFromScan(ocrCode: String) {
        updateAddProductDraft {
            AddProductDraft(
                ocrId = ocrCode.trim(),
                wasVerified = false
            )
        }
        openAddProductDialog()
    }

    fun dismissAddProductDialog() {
        _isAddProductDialogVisible.value = false
        savedStateHandle[KEY_DIALOG_VISIBLE] = false
    }

    fun cancelAddProductDialog() {
        clearAddProductDraft()
        dismissAddProductDialog()
    }

    fun updateAddProductDraft(transform: (AddProductDraft) -> AddProductDraft) {
        _addProductDraft.update { current ->
            val updated = transform(current)
            persistDraft(updated)
            updated
        }
    }

    fun verifyAddProductDraft() {
        viewModelScope.launch {
            val draft = _addProductDraft.value
            val existing = getProductByNameAndCategoryUseCase(draft.name.trim(), draft.category)
            updateAddProductDraft {
                it.copy(
                    wasVerified = true,
                    existingProductId = existing?.id,
                    stock = "",
                    minStock = "15"
                )
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = getProductsUseCase()
                updateCatalogProducts(products)
                _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
                else CatalogUiState.Success(products = products)
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Error al cargar productos")
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = searchProductsUseCase(query)
                updateCatalogProducts(products)
                _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
                else CatalogUiState.Success(products = products, searchQuery = query)
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Error al buscar")
            }
        }
    }

    fun filterByCategory(category: ProductCategory) {
        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading
            try {
                val products = filterProductsByCategoryUseCase(category)
                updateCatalogProducts(products)
                _uiState.value = if (products.isEmpty()) CatalogUiState.Empty
                else CatalogUiState.Success(products = products, activeFilter = category)
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(e.message ?: "Error al filtrar")
            }
        }
    }

    fun saveManualProduct(
        name: String,
        category: ProductCategory,
        stockValue: Int,
        minimumStock: Int,
        ocrIdentifier: String?,
        currentUser: AppUser?
    ) {
        if (currentUser?.role == UserRole.AUDITOR) {
            _uiState.value = CatalogUiState.Error(
                "El rol de auditor no tiene permiso para modificar el inventario."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = CatalogUiState.Loading

            try {
                val cleanName = name.trim()
                val existingProduct = getProductByNameAndCategoryUseCase(cleanName, category)

                val savedProduct = if (existingProduct != null) {
                    val newStock = existingProduct.currentStock + stockValue

                    if (newStock < 0) {
                        _uiState.value = CatalogUiState.Error(
                            "No se puede deducir más stock del disponible."
                        )
                        return@launch
                    }

                    val updatedProduct = existingProduct.copy(
                        currentStock = newStock,
                        lastUpdated = System.currentTimeMillis(),
                        isSynced = false
                    )

                    val updated = updateProductUseCase(updatedProduct)

                    addStockMovementUseCase(
                        StockMovement(
                            id = UUID.randomUUID().toString(),
                            productId = existingProduct.id,
                            movementType = if (stockValue >= 0) MovementType.ENTRY else MovementType.EXIT,
                            quantity = kotlin.math.abs(stockValue),
                            userId = currentUser?.id ?: "offline-user",
                            userName = currentUser?.name ?: "Usuario offline",
                            timestamp = System.currentTimeMillis(),
                            isSynced = false,
                            productName = existingProduct.name
                        )
                    )

                    updated
                } else {
                    val newProduct = com.example.myapplication.domain.model.Product(
                        id = UUID.randomUUID().toString(),
                        name = cleanName,
                        category = category,
                        description = "Producto registrado en bodega",
                        currentStock = stockValue,
                        minimumStock = minimumStock,
                        imageUrl = null,
                        ocrIdentifier = ocrIdentifier?.ifBlank { null },
                        lastUpdated = System.currentTimeMillis(),
                        isSynced = false
                    )

                    val created = addProductUseCase(newProduct)

                    created?.let {
                        addStockMovementUseCase(
                            StockMovement(
                                id = UUID.randomUUID().toString(),
                                productId = it.id,
                                movementType = MovementType.ENTRY,
                                quantity = stockValue,
                                userId = currentUser?.id ?: "offline-user",
                                userName = currentUser?.name ?: "Usuario offline",
                                timestamp = System.currentTimeMillis(),
                                isSynced = false,
                                productName = cleanName
                            )
                        )
                    }

                    created
                }

                if (savedProduct != null) {
                    val products = getProductsUseCase()
                    val pending = products.count { !it.isSynced }

                    clearAddProductDraft()
                    dismissAddProductDialog()

                    updateCatalogProducts(products)
                    _uiState.value = CatalogUiState.Success(
                        products = products,
                        isOffline = pending > 0,
                        pendingSyncCount = pending,
                        message = if (existingProduct != null)
                            "Stock actualizado correctamente."
                        else
                            "Producto agregado correctamente."
                    )
                } else {
                    _uiState.value = CatalogUiState.Error("No se pudo guardar el producto.")
                }
            } catch (e: Exception) {
                _uiState.value = CatalogUiState.Error(
                    e.message ?: "Error al guardar producto"
                )
            }
        }
    }

    private fun updateCatalogProducts(products: List<Product>) {
        if (products.isNotEmpty()) {
            _catalogProducts.value = products
        }
    }

    private fun clearAddProductDraft() {
        val empty = AddProductDraft()
        _addProductDraft.value = empty
        persistDraft(empty)
    }

    private fun restoreDraft(): AddProductDraft {
        val categoryName = savedStateHandle.get<String>(KEY_CATEGORY)
        val category = categoryName?.let { name ->
            runCatching { ProductCategory.valueOf(name) }.getOrNull()
        } ?: ProductCategory.STICKER_INDIVIDUAL

        return AddProductDraft(
            name = savedStateHandle.get<String>(KEY_NAME).orEmpty(),
            category = category,
            stock = savedStateHandle.get<String>(KEY_STOCK).orEmpty(),
            minStock = savedStateHandle.get<String>(KEY_MIN_STOCK) ?: "15",
            ocrId = savedStateHandle.get<String>(KEY_OCR_ID).orEmpty(),
            wasVerified = savedStateHandle.get<Boolean>(KEY_WAS_VERIFIED) ?: false,
            existingProductId = savedStateHandle.get<String>(KEY_EXISTING_PRODUCT_ID)
        )
    }

    private fun persistDraft(draft: AddProductDraft) {
        savedStateHandle[KEY_NAME] = draft.name
        savedStateHandle[KEY_CATEGORY] = draft.category.name
        savedStateHandle[KEY_STOCK] = draft.stock
        savedStateHandle[KEY_MIN_STOCK] = draft.minStock
        savedStateHandle[KEY_OCR_ID] = draft.ocrId
        savedStateHandle[KEY_WAS_VERIFIED] = draft.wasVerified
        savedStateHandle[KEY_EXISTING_PRODUCT_ID] = draft.existingProductId
    }

    companion object {
        private const val KEY_DIALOG_VISIBLE = "add_product_dialog_visible"
        private const val KEY_NAME = "add_product_name"
        private const val KEY_CATEGORY = "add_product_category"
        private const val KEY_STOCK = "add_product_stock"
        private const val KEY_MIN_STOCK = "add_product_min_stock"
        private const val KEY_OCR_ID = "add_product_ocr_id"
        private const val KEY_WAS_VERIFIED = "add_product_was_verified"
        private const val KEY_EXISTING_PRODUCT_ID = "add_product_existing_id"
    }
}
