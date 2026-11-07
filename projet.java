import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// ==========================================

interface Urgences {
    void traiterUrgence();
}

interface Archivable {
    String genererIdentifiantArchive();
}


// ==========================================
class CabinetException extends Exception {
    public CabinetException(String message) {
        super(message);
    }
}

class RendezVousIntrouvableException extends CabinetException {
    public RendezVousIntrouvableException(String id) {
        super("Erreur : Le rendez-vous avec l'ID '" + id + "' n'existe pas.");
    }
}

class CapaciteDepasseeException extends CabinetException {
    public CapaciteDepasseeException(String msg) {
        super(msg);
    }
}


// ==========================================
abstract class Personne implements Archivable {
    private  String id; 
    private String nom;
    private String prenom;

    public Personne(String id, String nom, String prenom) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getId() { 
        return id;
     }
    public String getNom() { 
        return nom; 
    }
    public String getPrenom() { 
        return prenom;
     }
    
    @Override
    public final String genererIdentifiantArchive() { 
        return "ARC-" + id + "-" + nom.toUpperCase();
    }

    @Override
    public String toString() {
        return nom.toUpperCase() + " " + prenom + " (ID: " + id + ")";
    }
}

class Patient extends Personne {
    private String telephone;

    public Patient(String id, String nom, String prenom, String telephone) {
        super(id, nom, prenom);
        this.telephone = telephone;
    }

    public String getTelephone() { 
        return telephone; 
    }
}

class Medecin extends Personne {
    private String specialite;

    public Medecin(String id, String nom, String prenom, String specialite) {
        super(id, nom, prenom);
        this.specialite = specialite;
    }

    public String getSpecialite() { 
        return specialite;
     }
}

class MedecinUrgentiste extends Medecin implements Urgences {
    public MedecinUrgentiste(String id, String nom, String prenom) {
        super(id, nom, prenom, "Urgentiste / Réanimateur");
    }

    @Override
    public void traiterUrgence() {
        System.out.println("[URGENCE] Dr. " + getNom() + " déploie le protocole de réanimation immédiat !");
    }
}


// ==========================================
class RendezVous {
    private String idRdv;
    private LocalDateTime dateHeure;
    private Patient patient;
    private Medecin medecin;
    private boolean estEffectue;

    public RendezVous(String idRdv, LocalDateTime dateHeure, Patient patient, Medecin medecin) {
        this.idRdv = idRdv;
        this.dateHeure = dateHeure;
        this.patient = patient;
        this.medecin = medecin;
        this.estEffectue = false;
    }

    public String getIdRdv() { 
        return idRdv; 
    }
    public Patient getPatient() {
         return patient; 
        }
    public Medecin getMedecin() { 
        return medecin; 
    }
    public boolean estEffectue() { 
        return estEffectue; 
    }
    public void marquerEffectue() { 
        this.estEffectue = true; 
    }

    @Override
    public String toString() {
        return "[RDV " + idRdv + "] " + dateHeure + " | Patient: " + patient.getNom() + " -> Dr. " + medecin.getNom();
    }
}

class Consultation {
    private final RendezVous rendezVous;
    private String diagnostic;
    private final List<String> ordonnance; 

    public Consultation(RendezVous rendezVous, String diagnostic) {
        this.rendezVous = rendezVous;
        this.diagnostic = diagnostic;
        this.ordonnance = new ArrayList<>();
        this.rendezVous.marquerEffectue();
    }

    public void ajouterMedicament(String medicament) {
        this.ordonnance.add(medicament);
    }

    public void afficherFiche() {
        System.out.println("\n--- FICHES DE CONSULTATION ---");
        System.out.println("Patient : " + rendezVous.getPatient());
        System.out.println("Médecin : Dr. " + rendezVous.getMedecin().getNom() + " (" + rendezVous.getMedecin().getSpecialite() + ")");
        System.out.println("Diagnostic : " + diagnostic);
        System.out.println("Ordonnance : " + String.join(", ", ordonnance));
    }
}


// ==========================================
class CabinetMedical {
    private final List<Personne> personnelEtPatients = new ArrayList<>(); 
    private final Map<String, RendezVous> carnetRendezVous = new HashMap<>();
    private final List<Consultation> historiqueConsultations = new ArrayList<>();

    private static final int MAX_RDV_JOUR = 100;

    public void inscrireIndividu(Personne p) {
        personnelEtPatients.add(p);
        System.out.println("Enregistrement archive : " + p.genererIdentifiantArchive());
    }

    public void planifierRendezVous(String id, LocalDateTime date, Patient p, Medecin m) throws CapaciteDepasseeException {
        if (carnetRendezVous.size() >= MAX_RDV_JOUR) {
            throw new CapaciteDepasseeException("Le cabinet est complet pour aujourd'hui.");
        }
        RendezVous rdv = new RendezVous(id, date, p, m);
        carnetRendezVous.put(id, rdv);
        System.out.println("Rendez-vous enregistré : " + id);
    }

    public void planifierRendezVous(String id, Patient p, Medecin m) throws CapaciteDepasseeException {
        planifierRendezVous(id, LocalDateTime.now(), p, m);
    }

    public void traiterConsultation(String idRdv, String diagnostic, List<String> medicaments) throws RendezVousIntrouvableException {
        if (!carnetRendezVous.containsKey(idRdv)) {
            throw new RendezVousIntrouvableException(idRdv);
        }

        RendezVous rdv = carnetRendezVous.get(idRdv);
        Consultation cons = new Consultation(rdv, diagnostic);
        medicaments.forEach(cons::ajouterMedicament);
        
        historiqueConsultations.add(cons);
        cons.afficherFiche();
    }

    public void afficherToutesLesPersonnes() {
        System.out.println("\n--- REGISTRE DU CABINET  ---");
        for (Personne p : personnelEtPatients) {
            System.out.println("- [" + p.getClass().getSimpleName() + "] " + p);
        }
    }
}


// ==========================================
public class projet {
    public static void main(String[] args) {
        CabinetMedical cabinet = new CabinetMedical();

        Patient pat = new Patient("P10", "Chabane", "Samir", "0711223344");
        Medecin med = new Medecin("M05", "Ben Ali", "Yassine", "Cardiologue");
        MedecinUrgentiste urgentiste = new MedecinUrgentiste("M99", "Gérard", "Julie");

        cabinet.inscrireIndividu(pat);
        cabinet.inscrireIndividu(med);
        cabinet.inscrireIndividu(urgentiste);

        cabinet.afficherToutesLesPersonnes();

        System.out.println("\n--- EXÉCUTION DU FLUX MÉTIER ---");
        try {
            urgentiste.traiterUrgence();

            cabinet.planifierRendezVous("RDV-01", pat, med); 

            List<String> ordonnance = List.of("Cardiocalm 10mg", "Repos complet");
            cabinet.traiterConsultation("RDV-01", "maladie cardiologue", ordonnance);

            System.out.println("\n--- TEST SÉCURITÉ DE L'APPLICATION ---");
            cabinet.traiterConsultation("RDV-FAKE", "Erreur", List.of());

        } catch (RendezVousIntrouvableException e) {
            System.err.println("Alerte Métier : " + e.getMessage());
        } catch (CapaciteDepasseeException e) {
            System.err.println("Alerte Système : " + e.getMessage());
        } finally {
            System.out.println("\n--- FIN DU PROGRAMME ---");
        }
    }
}
