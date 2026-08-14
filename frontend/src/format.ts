/**
 * Utilitários de formatação para exibição ao usuário.
 */

const currencyFormatter = new Intl.NumberFormat('pt-BR', {
  style: 'currency',
  currency: 'BRL',
});

/** Formata um valor numérico como moeda brasileira (R$ 1.234,56). */
export function formatCurrency(value: number): string {
  return currencyFormatter.format(value);
}
