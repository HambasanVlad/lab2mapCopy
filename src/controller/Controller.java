package controller;

import exception.MyException;
import model.PrgState;
import model.value.RefValue;
import model.value.Value;
import repository.IRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        // FIX: Inițializăm executorul AICI pentru a fi disponibil oricând, inclusiv în GUI
        this.executor = Executors.newFixedThreadPool(2);
    }

    public void setDisplayFlag(boolean value) {
        this.displayFlag = value;
    }

    public IRepository getRepo() {
        return repo;
    }

    // --- GARBAGE COLLECTOR OPTIMIZAT ---
    Map<Integer, Value> safeGarbageCollector(List<Integer> symTableAddr, Map<Integer, Value> heap) {
        List<Integer> referencedAddresses = new java.util.ArrayList<>(symTableAddr);

        // Iterăm pentru a găsi referințele indirecte (Heap -> Heap)
        boolean change = true;
        while (change) {
            change = false;
            List<Integer> newAddresses = heap.entrySet().stream()
                    .filter(e -> referencedAddresses.contains(e.getKey())) // Doar nodurile deja accesibile
                    .map(Map.Entry::getValue)
                    .filter(v -> v instanceof RefValue)
                    .map(v -> ((RefValue) v).getAddr())
                    .filter(addr -> !referencedAddresses.contains(addr)) // Doar ce nu am colectat deja
                    .collect(Collectors.toList());

            if (!newAddresses.isEmpty()) {
                referencedAddresses.addAll(newAddresses);
                change = true;
            }
        }

        // Păstrăm în Heap doar ce e în lista finală de adrese valide
        return heap.entrySet().stream()
                .filter(e -> referencedAddresses.contains(e.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    // --- CONCURRENCY METHODS ---
    public List<PrgState> removeCompletedPrg(List<PrgState> inPrgList) {
        return inPrgList.stream()
                .filter(PrgState::isNotCompleted)
                .collect(Collectors.toList());
    }

    public void oneStepForAllPrg(List<PrgState> prgList) throws InterruptedException {
        // FIX DE SIGURANȚĂ: Dacă executorul a fost închis sau e null, îl recreăm
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(2);
        }

        // 1. Logare stare înainte de execuție
        prgList.forEach(prg -> {
            try {
                repo.logPrgStateExec(prg);
                if (displayFlag) System.out.println(prg.toString());
            } catch (MyException e) {
                System.out.println("Eroare la logare: " + e.getMessage());
            }
        });

        // 2. Pregătire callables
        List<Callable<PrgState>> callList = prgList.stream()
                .map((PrgState p) -> (Callable<PrgState>) (p::oneStep))
                .collect(Collectors.toList());

        // 3. Execuție concurentă
        List<PrgState> newPrgList = executor.invokeAll(callList).stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (ExecutionException | InterruptedException e) {
                        // Ignorăm erorile de tip "thread terminat"
                        if (!(e.getCause() instanceof MyException)) {
                            System.out.println("Eroare thread: " + e.getMessage());
                        }
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 4. Adăugare fire noi
        prgList.addAll(newPrgList);

        // 5. Logare stare după execuție
        prgList.forEach(prg -> {
            try {
                repo.logPrgStateExec(prg);
                if (displayFlag) System.out.println(prg.toString());
            } catch (MyException e) {
                System.out.println("Eroare la logare finală: " + e.getMessage());
            }
        });

        repo.setPrgList(prgList);
    }

    public void allStep() throws InterruptedException {
        // Asigurăm că executorul există
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(2);
        }

        List<PrgState> prgList = removeCompletedPrg(repo.getPrgList());

        while (prgList.size() > 0) {
            // Garbage Collector apelat corect
            List<Integer> symTableAddresses = prgList.stream()
                    .map(p -> p.getSymTable().getContent().values())
                    .flatMap(Collection::stream)
                    .filter(v -> v instanceof RefValue)
                    .map(v -> ((RefValue) v).getAddr())
                    .collect(Collectors.toList());

            PrgState firstPrg = prgList.get(0);
            firstPrg.getHeap().setContent(
                    safeGarbageCollector(symTableAddresses, firstPrg.getHeap().getContent())
            );

            oneStepForAllPrg(prgList);
            prgList = removeCompletedPrg(repo.getPrgList());
        }

        // NOTĂ: Nu mai dăm shutdown aici pentru a nu strica GUI-ul dacă utilizatorul vrea să ruleze din nou.
        // Executorul se va închide când aplicația se termină complet.
        repo.setPrgList(prgList);
    }

    // Metodă opțională pentru a închide corect resursele la final de tot (dacă e nevoie)
    public void shutdownExecutor() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }
    }
}