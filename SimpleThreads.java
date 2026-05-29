public class SimpleThreads {

    static void threadMessage(String message) {
        String threadName = Thread.currentThread().getName();
        System.out.format("%s: %s%n", threadName, message);
    }

    private static class MessageLoop implements Runnable {
        public void run() {
            String importantInfo[] = {
                    "Mares eat oats",
                    "Does eat oats",
                    "Little lambs eat ivy",
                    "A kid will eat ivy too"
            };
            try {
                for (int i = 0; i < importantInfo.length; i++) {
                    Thread.sleep(4000);
                    threadMessage(importantInfo[i]);
                }
            } catch (InterruptedException e) {
                threadMessage("I wasn't done!");
            }
        }
    }

    private static class PrimeCalculator implements Runnable {

        private final long upperBound;

        PrimeCalculator(long upperBound) {
            this.upperBound = upperBound;
        }

        private boolean isPrime(long n) {
            if (n < 2) return false;
            if (n == 2) return true;
            if (n % 2 == 0) return false;
            for (long i = 3; i * i <= n; i += 2) {
                if (n % i == 0) return false;
            }
            return true;
        }

        public void run() {
            threadMessage("Iniciando cálculo de primos até " + upperBound);
            long count = 0;
            long lastPrime = -1;

            for (long candidate = 2; candidate <= upperBound; candidate++) {

                if (Thread.interrupted()) {
                    threadMessage("Interrompido! Último primo encontrado: "
                            + lastPrime + " | Total encontrado até agora: " + count);
                    return;
                }

                if (isPrime(candidate)) {
                    count++;
                    lastPrime = candidate;
                }
            }

            threadMessage("Concluído! Primos encontrados: " + count
                    + " | Maior primo: " + lastPrime);
        }
    }

    public static void main(String[] args) throws InterruptedException {

        long patience = 1000L * 60 * 60;

        if (args.length > 0) {
            try {
                patience = Long.parseLong(args[0]) * 1000;
            } catch (NumberFormatException e) {
                System.err.println("O argumento deve ser um inteiro (segundos).");
                System.exit(1);
            }
        }

        threadMessage("Iniciando thread MessageLoop");
        Thread messageThread = new Thread(new MessageLoop(), "MessageLoop");
        messageThread.start();

        long upperBound = 10_000_000L;
        threadMessage("Iniciando thread PrimeCalculator (limite=" + upperBound + ")");
        long startTime = System.currentTimeMillis();
        Thread primeThread = new Thread(new PrimeCalculator(upperBound), "PrimeCalculator");
        primeThread.start();

        threadMessage("Aguardando as threads terminarem...");

        while (primeThread.isAlive()) {
            threadMessage("PrimeCalculator ainda em execução...");
            primeThread.join(1000);

            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed > patience && primeThread.isAlive()) {
                threadMessage("Tempo esgotado! Interrompendo PrimeCalculator...");
                primeThread.interrupt();
                primeThread.join();
            }
        }

        threadMessage("PrimeCalculator finalizada.");

        messageThread.join();
        threadMessage("Todas as threads concluídas!");
    }
}