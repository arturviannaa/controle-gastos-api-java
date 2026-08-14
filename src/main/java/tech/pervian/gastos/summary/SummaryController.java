package tech.pervian.gastos.summary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.pervian.gastos.summary.dto.SummaryResponse;

@Tag(name = "Totais", description = "Receitas, despesas e saldo por pessoa")
@RestController
@RequestMapping("/api/summary")
public class SummaryController {

    private final SummaryService service;

    public SummaryController(SummaryService service) {
        this.service = service;
    }

    @Operation(summary = "Totais de cada pessoa e o total geral")
    @GetMapping
    public SummaryResponse consultar() {
        return service.consultar();
    }
}
