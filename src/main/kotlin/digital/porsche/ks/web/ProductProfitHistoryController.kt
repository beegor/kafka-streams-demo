package digital.porsche.ks.web

import digital.porsche.ks.model.ProductProfitHistory
import digital.porsche.ks.model.ProductReport
import digital.porsche.ks.model.ProductsOffered
import digital.porsche.ks.service.ProductProfitHistoryManager
import digital.porsche.ks.streams.ProfitCalculator
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.math.BigDecimal
import java.time.Instant

@Controller
class ProductProfitHistoryController(
    private val productProfitHistoryManager: ProductProfitHistoryManager
) {

    @GetMapping("/profit-history")
    fun showProfitHistory( @RequestParam(required = false) timeFrom: Long?,
                           @RequestParam(required = false) timeTo: Long?,
                           model: Model ): String {
        val from = timeFrom  ?: (Instant.now().toEpochMilli() - 5000)
        val to = timeTo  ?: (Instant.now().toEpochMilli() + 10000)
        model.addAttribute("timeFrom", from)
        model.addAttribute("timeTo", to)
        model.addAttribute("productLabels" , ProductsOffered.get().map { it.name }.toTypedArray())
        return "product-profit-history"
    }


    @GetMapping("/profit-history-data")
    @ResponseBody
    fun getProfitHistory(@RequestParam timeFrom: Long, @RequestParam timeTo: Long): Map<String, Any> {

        val labels = ProductsOffered.get().map { it.name }
        val profits: List<ProductProfitHistory> = productProfitHistoryManager.getProfitHistory(timeFrom, timeTo)
        val profitByProduct: Map<String, BigDecimal> = profits.groupBy { it.productId }.mapValues { it.value.sumOf { it.profit } }
        val data = ProductsOffered.get().map { profitByProduct[it.id] ?: BigDecimal.ZERO }

        return mapOf<String, Any>(
            "productLabels" to labels.toTypedArray(),
            "productProfitValues" to data.toTypedArray()
        )
    }








}