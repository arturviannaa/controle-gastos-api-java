package tech.pervian.gastos.person.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// mensagens em portugues porque chegam ao usuario final pela tela
public record CreatePersonRequest(

        @Schema(example = "Artur")
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres.")
        String name,

        @Schema(example = "27")
        @Min(value = 0, message = "A idade deve estar entre 0 e 150.")
        @Max(value = 150, message = "A idade deve estar entre 0 e 150.")
        int age
) {
}
