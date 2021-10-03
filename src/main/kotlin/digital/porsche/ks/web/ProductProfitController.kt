package digital.porsche.ks.web

import digital.porsche.ks.model.ProductReport
import digital.porsche.ks.model.ProductsOffered
import digital.porsche.ks.streams.ProfitCalculator
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import java.math.BigDecimal

@Controller
class ProductProfitController(
    private val profitCalculator: ProfitCalculator
) {

    @GetMapping("/profit")
    fun showProfit( model: Model ): String {
        model.addAttribute("productLabels" , ProductsOffered.get().map { it.name }.toTypedArray())
        return "product-profit"
    }


    @GetMapping("/profit-data")
    @ResponseBody
    fun getProfitData(): Map<String, Any> {

        val labels = ProductsOffered.get().map { it.name }
        val profits: Map<String, ProductReport> = profitCalculator.getProfits()
        val data = ProductsOffered.get().map { profits[it.id]?.profit ?: BigDecimal.ZERO }

        return mapOf<String, Any>(
            "productLabels" to labels.toTypedArray(),
            "productProfitValues" to data.toTypedArray()
        )
    }








}