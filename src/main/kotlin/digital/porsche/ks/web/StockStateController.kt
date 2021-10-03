package digital.porsche.ks.web

import digital.porsche.ks.model.ProductsOffered
import digital.porsche.ks.service.StockManager
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class StockStateController(
    private val stockManager: StockManager
) {

    @GetMapping("/stock-state/{shopId}")
    fun showStockState( @PathVariable shopId: String, model: Model ): String {
        model.addAttribute("stockLabels" , ProductsOffered.get().map { it.name }.toTypedArray())
        model.addAttribute("shopId", shopId)
        return "stock-state"
    }


    @GetMapping("/stock-state-data/{shopId}")
    @ResponseBody
    fun getStockState(@PathVariable shopId: String): Map<String, Any> {

        val labels = ProductsOffered.get().map { it.name }
        val data = ProductsOffered.get().map { stockManager.getStockState(shopId)?.productState?.get(it.id) ?: 0 }

        return mapOf<String, Any>(
            "stockLabels" to labels.toTypedArray(),
            "stockStateValues" to data.toTypedArray()
        )
    }








}