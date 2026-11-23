package br.edu.financeiro.ui;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/** O trabalho de banco ocorre fora da thread que desenha a interface. */
public final class TarefaTela {
    private TarefaTela() { }
    public static <T> void executar(Callable<T> trabalho, Consumer<T> sucesso,
                                   Consumer<Throwable> falha, Consumer<Boolean> ocupacao) {
        ocupacao.accept(true);
        new SwingWorker<T, Void>() {
            @Override protected T doInBackground() throws Exception { return trabalho.call(); }
            @Override protected void done() {
                ocupacao.accept(false);
                try { sucesso.accept(get()); }
                catch (InterruptedException erro) { Thread.currentThread().interrupt(); falha.accept(erro); }
                catch (ExecutionException erro) { falha.accept(erro.getCause()); }
            }
        }.execute();
    }
}
