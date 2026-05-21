package com.sdd.marketplace.feature.product.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sdd.marketplace.domain.model.Product
import com.sdd.marketplace.domain.repository.AuthRepository
import com.sdd.marketplace.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class PostProductUiState(
    val step: Int = 1,
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val brand: String = "",
    val condition: String = "",
    val price: String = "0",
    val discountPrice: String = "0",
    val stockQuantity: Int = 1,
    val tags: List<String> = emptyList(),
    val selectedImages: List<Uri> = emptyList(),
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val deliveryOptions: List<String> = emptyList(),
    val returnPolicy: String = "",
    val isNegotiable: Boolean = false,
    val isNew: Boolean = true,
    val isBoosted: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

sealed class PostProductEvent {
    object PostSuccess : PostProductEvent()
    data class ShowError(val message: String) : PostProductEvent()
}

@HiltViewModel
class PostProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostProductUiState())
    val uiState: StateFlow<PostProductUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<PostProductEvent>()
    val events: SharedFlow<PostProductEvent> = _events.asSharedFlow()

    fun updateTitle(title: String) = _uiState.update { it.copy(title = title) }
    fun updateDescription(desc: String) = _uiState.update { it.copy(description = desc) }
    fun updateCategory(cat: String) = _uiState.update { it.copy(category = cat) }
    fun updateBrand(brand: String) = _uiState.update { it.copy(brand = brand) }
    fun updateCondition(cond: String) = _uiState.update { it.copy(condition = cond) }
    fun updatePrice(price: String) = _uiState.update { it.copy(price = price) }
    fun updateDiscountPrice(p: String) = _uiState.update { it.copy(discountPrice = p) }
    fun updateStock(qty: Int) = _uiState.update { it.copy(stockQuantity = qty) }
    fun updateLocation(loc: String, lat: Double? = null, lng: Double? = null) =
        _uiState.update { it.copy(location = loc, latitude = lat, longitude = lng) }
    fun updateReturnPolicy(policy: String) = _uiState.update { it.copy(returnPolicy = policy) }
    fun toggleNegotiable() = _uiState.update { it.copy(isNegotiable = !it.isNegotiable) }
    fun toggleNew() = _uiState.update { it.copy(isNew = !it.isNew) }
    fun toggleBoosted() = _uiState.update { it.copy(isBoosted = !it.isBoosted) }

    fun addImage(uri: Uri) {
        val current = _uiState.value.selectedImages.toMutableList()
        if (current.size < 10) { current.add(uri); _uiState.update { it.copy(selectedImages = current) } }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { it.copy(selectedImages = it.selectedImages.filter { img -> img != uri }) }
    }

    fun addTag(tag: String) {
        val tags = _uiState.value.tags.toMutableList()
        if (tags.size < 10 && !tags.contains(tag)) { tags.add(tag) }
        _uiState.update { it.copy(tags = tags) }
    }

    fun removeTag(tag: String) = _uiState.update { it.copy(tags = it.tags.filter { t -> t != tag }) }

    fun toggleDeliveryOption(option: String) {
        val options = _uiState.value.deliveryOptions.toMutableList()
        if (options.contains(option)) options.remove(option) else options.add(option)
        _uiState.update { it.copy(deliveryOptions = options) }
    }

    fun nextStep() = _uiState.update { it.copy(step = it.step + 1) }
    fun prevStep() = _uiState.update { it.copy(step = maxOf(1, it.step - 1)) }

    fun submitProduct() = viewModelScope.launch {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId() ?: run {
            _events.emit(PostProductEvent.ShowError("Please sign in to post a product"))
            return@launch
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        val imagePaths = state.selectedImages.mapNotNull { uri ->
            uriToFile(uri)?.absolutePath
        }

        val product = Product(
            id = "", title = state.title, description = state.description,
            price = state.price.toDoubleOrNull() ?: 0.0,
            discountPrice = state.discountPrice.toDoubleOrNull()?.takeIf { it > 0 },
            currency = "INR", category = state.category, brand = state.brand.ifBlank { null },
            condition = state.condition, stockQuantity = state.stockQuantity,
            images = emptyList(), tags = state.tags, attributes = emptyMap(),
            sellerId = userId, seller = null, location = state.location.ifBlank { null },
            latitude = state.latitude, longitude = state.longitude,
            deliveryOptions = state.deliveryOptions, returnPolicy = state.returnPolicy.ifBlank { null },
            isNegotiable = state.isNegotiable, isFeatured = false,
            isBoosted = state.isBoosted, isNew = state.isNew, isSold = false,
            viewCount = 0, favoriteCount = 0, rating = 0.0, reviewCount = 0,
            createdAt = "", updatedAt = ""
        )

        productRepository.createProduct(product, imagePaths)
            .onSuccess { _events.emit(PostProductEvent.PostSuccess) }
            .onFailure {
                _uiState.update { s -> s.copy(error = it.message) }
                _events.emit(PostProductEvent.ShowError(it.message ?: "Failed to post product"))
            }
        _uiState.update { it.copy(isLoading = false) }
    }

    private fun uriToFile(uri: Uri): File? = try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { output -> inputStream.copyTo(output) }
        inputStream.close()
        tempFile
    } catch (e: Exception) { null }
}
