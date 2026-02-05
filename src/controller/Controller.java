package controller;

import exception.MyException;
import model.PrgState;
import model.value.RefValue;
import model.value.Value;
import repository.IRepository;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Controller {
    private IRepository repo;
    private boolean displayFlag = true;
    private ExecutorService executor;

    public Controller(IRepository repo) {
        this.repo = repo;
        // Inițializăm executorul la start
        this.executor = Executors.newFixedThreadPool(2);
    }

    public void setDisplayFlag(boolean value) {
        this.displayFlag = value;
    }

    public IRepository getRepo() {
        return repo;
    }

    // --- GARBAGE COLLECTOR ---
    Map<Integer, Value> safeGarbageCollector(List<Integer> symTableAddr, Map<Integer, Value> heap) {
        List<Integer> referencedAddresses = new ArrayList<>(symTableAddr);
        boolean change = true;

        // Căutăm referințe indirecte (heap -> heap)
        while (change) {
            change = false;
            List<Integer> newAddresses = heap.entrySet().stream()
                    .filter(e -> referencedAddresses.contains(e.getKey()))
                    .map(Map.Entry::getValue)
                    .filter(v -> v instanceof RefValue)
                    .map(v -> ((RefValue) v).getAddr())
                    .filter(addr -> !referencedAddresses.contains(addr))
                    .collect(Collectors.toList());

            if (!newAddresses.isEmpty()) {
                referencedAddresses.addAll(newAddresses);
                change = true;
            }
        }

        return heap.entrySet().stream()
                .filter(e -> referencedAddresses.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<PrgState> removeCompletedPrg(List<PrgState> inPrgList) {
        return inPrgList.stream()
                .filter(PrgState::isNotCompleted)
                .collect(Collectors.toList());
    }

    public void oneStepForAllPrg(List<PrgState> prgList) throws InterruptedException {
        // Logare înainte de execuție (opțional, pentru debug)
        // prgList.forEach(p -> {try { repo.logPrgStateExec(p); } catch (MyException e) {}});

        // 1. PREGĂTIRE CALLABLES - FILTRU CORECTAT
        // Executăm DOAR programele care NU sunt terminate (au stiva ne-goală)
        List<Callable<PrgState>> callList = prgList.stream()
                .filter(p -> p.isNotCompleted()) // <--- AICI ERA PROBLEMA (fără !)
                .map((PrgState p) -> (Callable<PrgState>) (p::oneStep))
                .collect(Collectors.toList());

        // 2. EXECUȚIE CONCURENTĂ
        // invokeAll blochează până când toate thread-urile termină pasul curent
        List<PrgState> newPrgList = executor.invokeAll(callList).stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (ExecutionException | InterruptedException e) {
                        // Aici prindem erorile din thread-uri pentru a nu crăpa tot GUI-ul
                        System.out.println("Eroare execuție thread: " + e.getMessage());
                        if (e.getCause() instanceof MyException)
                            System.out.println("Detalii MyException: " + e.getCause().getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Eliminăm null-urile generate de erori sau instrucțiuni simple
                .collect(Collectors.toList());

        // 3. ADĂUGARE THREAD-URI NOI
        prgList.addAll(newPrgList);

        // 4. LOGARE STARE DUPĂ EXECUȚIE
        // Iterăm o copie sau lista direct, dar sincronizat logic de invokeAll
        prgList.forEach(prg -> {
            try {
                repo.logPrgStateExec(prg);
                if (displayFlag) System.out.println(prg.toString());
            } catch (MyException | ConcurrentModificationException e) {
                // Prindem CME doar ca să nu blocheze GUI-ul, deși nu ar trebui să mai apară
                System.out.println("Eroare la logare: " + e.getMessage());
            }
        });

        // 5. SALVARE ÎN REPO
        repo.setPrgList(prgList);
    }

    public void allStep() throws InterruptedException {
        executor = Executors.newFixedThreadPool(2);
        List<PrgState> prgList = removeCompletedPrg(repo.getPrgList());

        while (prgList.size() > 0) {
            // Garbage Collector
            List<Integer> symTableAddresses = prgList.stream()
                    .map(p -> p.getSymTable().getContent().values())
                    .flatMap(Collection::stream)
                    .filter(v -> v instanceof RefValue)
                    .map(v -> ((RefValue) v).getAddr())
                    .collect(Collectors.toList());

            if (!prgList.isEmpty()) {
                prgList.get(0).getHeap().setContent(
                        safeGarbageCollector(symTableAddresses, prgList.get(0).getHeap().getContent())
                );
            }

            oneStepForAllPrg(prgList);

            // Eliminăm programele terminate pentru bucla while,
            // dar ele rămân în repo pentru a fi văzute în GUI la final
            prgList = removeCompletedPrg(repo.getPrgList());
        }

        executor.shutdownNow();
        repo.setPrgList(prgList);
    }
}