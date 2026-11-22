package app;

public enum TipoTransacao {
    PIX,
    DEPOSITO,
    SAQUE,
    TRANSFERENCIA,
    /** Dinheiro que sai da conta para uma aposta no cassino (remetente = conta, sem destinatário). */
    APOSTA,
    /** Dinheiro que volta para a conta como prêmio do cassino (sem remetente, destinatário = conta). */
    PREMIO
}
