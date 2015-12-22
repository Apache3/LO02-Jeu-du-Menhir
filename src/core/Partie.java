package core;

import affich.Console;
import java.util.*;


/**
 * La classe Partie modélise le déroulement d'une partie.<br>
 * 
 * 
 *
 */
public abstract class Partie extends Observable{

    protected int nbrManches;
    protected int nbrMancheActuelle;
    protected int nbrTourActuel;
    protected Console console;
    protected Joueur joueurActif;
    protected TypeSaison saison;
    protected ArrayList<Joueur> listeJoueurs;
    protected LinkedList<CarteIngredient> listeCarteIng;
    protected boolean running;
    protected boolean tourRunning;
    protected Iterator<Joueur> joueurIterator;
    protected boolean partAvancee;
    
    public Partie(int nbJH,int nbJIA){
        console = Console.getInstance();
        
        listeJoueurs = new ArrayList<>();
        joueurIterator=listeJoueurs.iterator();
        listeCarteIng = new LinkedList<>();
        running =false;
        tourRunning = false;
        nbrMancheActuelle =1;
        nbrTourActuel = 1;
        saison = TypeSaison.PRINTEMPS;
        creerJoueur(nbJH,nbJIA);
        creerCartes();
    }
    public void lancerPartie(){
        initPartie();
        do{
            if(!tourRunning){
                initManche();
                console.displayNbManche(this);
            }
            if(partAvancee){
                if(saison==TypeSaison.PRINTEMPS)
                    for(Joueur j:listeJoueurs){
                        joueurActif = j;
                        if(joueurActif.choixAllie()){
                            distribCarteAl(j);
                            console.displayTypeAllie(joueurActif);
                        }
                        else
                            joueurActif.setNbrGraines(2);
                    }
            }
            
                    
            for(Joueur j:listeJoueurs){
                joueurActif = j;
                if(partAvancee&&saison!=TypeSaison.PRINTEMPS)
                    if(joueurActif.hasAllie()&&joueurActif.getCarteAl() instanceof CarteTaupe)
                        joueurActif.jouerAllie(this);
                joueurActif.jouerTour(this);
                if(partAvancee)
                    if(joueurActif.getChoixJoueur().getAction()==TypeAction.FARFADET)
                        if(joueurActif.getChoixJoueur().getCible().hasAllie())
                            if(joueurActif.getChoixJoueur().getCible().getCarteAl() instanceof CarteChien)
                                joueurActif.getChoixJoueur().getCible().deciderReaction(this);
                
                console.displayAction(joueurActif,saison);
                joueurActif.jouerCarte(saison);
                joueurActif.setChoixJoueur(new ChoixJoueur());

            }
            nextTour();
            if(!tourRunning){
                this.recupCartes();
                if(partAvancee)
                    console.displayFinManche(listeJoueurs);
            } 
        }while(running);
        listeJoueurs.add(listeJoueurs.remove(0));
        console.displayGagnant(this.getPalmares());
        
    }
    protected void initPartie(){
        for(Joueur j:listeJoueurs){
            j.setNbrPoints(0);
            j.setNbrGraines(0);
            j.setNbrMenhir(nbrManches);
        }
    }
    public void initManche(){
        this.tourRunning = true;
        this.running =true;
        saison=saison.initSaison();
        Collections.shuffle(this.listeCarteIng);
        distribCartes();
        for(Joueur j:listeJoueurs){
            j.setNbrGraines(0);
            j.setNbrMenhir(0);
        }
        
    }
    public void distribCarteAl(Joueur j){
        return;
    }
    
    /** Getters et Setters
     * @return  */
    
    public int getNbrManche(){
    	return this.nbrMancheActuelle;
    }
    public int getNbrTour(){
        return this.nbrTourActuel;
    }
    public TypeSaison getSaison(){
        return this.saison;
    }
    public Joueur getJoueurActif(){
        return joueurActif;
    }
    public ArrayList<Joueur> getListeJoueurs(){
        return listeJoueurs;
    }
    
    public ArrayList<Joueur> getPalmares(){
        ArrayList<Joueur> palmares = new ArrayList<>();
        ArrayList<Joueur> temp = new ArrayList<>();
        temp.addAll(listeJoueurs);
        
        Joueur j = temp.get(0);
        while(!temp.isEmpty()){
            int nbPtsMax=-1;
            for(Iterator<Joueur> iter = temp.iterator(); iter.hasNext();)
            {
                Joueur k =iter.next();
                if(k.getNbrPoints()>nbPtsMax){
                    nbPtsMax = k.getNbrPoints();
                    j=k;
                }
            }
            if(palmares.isEmpty()){
                palmares.add(j);
                temp.remove(j);
            }
            else{
                if((palmares.get(palmares.size()-1).getNbrMenhirs())==j.getNbrMenhirs()){
                    if(palmares.get(palmares.size()-1).getNbrGraines()<j.getNbrGraines()){
                        palmares.add(palmares.size()-1, j);
                        temp.remove(j);
                    }
                    else{
                        palmares.add(j);
                        temp.remove(j);
                    }
                }
                else{
                    palmares.add(j);
                    temp.remove(j);
                }
            }
        }
        return palmares;
    }
    public Joueur getJoueurMaxMenhir(){
        int nbMax=-1;
        Joueur cible = null;
        while(cible==null){
            for(Joueur j:listeJoueurs)
            {
                if(j!=joueurActif&&j.getNbrMenhirs()>nbMax)
                {
                    nbMax=j.getNbrMenhirs();
                    cible = j;

                }
            }
        }
        return cible;
    }
    public Joueur getJoueurMaxGraines(){
        int nbMax=-1;
        Joueur cible=null;
        while(cible==null){
            for(Joueur j:listeJoueurs)
            {
                if(j!=joueurActif&&j.getNbrGraines()>nbMax)
                {
                    nbMax=j.getNbrGraines();
                    cible=j;
                }
            }
        }
        return cible;
    }
    public boolean isRunning(){
        return running;
    }
    public boolean isTourRunning(){
        return tourRunning;
    }
    public void setNbrManches(int n){
        nbrManches=n;
    }
   
    
    
    public void nextManche(){
        nbrMancheActuelle++;
        listeJoueurs.add(listeJoueurs.remove(0));
        tourRunning=false;
        if(nbrMancheActuelle>nbrManches){
            running = false;
            nbrMancheActuelle = 1;
                
        }
        
    }
 
    public void nextTour(){
        nbrTourActuel++;
        saison=saison.next();
        if(nbrTourActuel>4){
            nbrTourActuel=1;
            nextManche();
        }
    }
    private void distribCartes() {
        for(Joueur j:listeJoueurs){
            for(int i=0;i<4;i++){
                j.addCarteIng(this.listeCarteIng.pop());
            }   
        }
    }
    private void creerJoueur(int nbJH, int nbJIA){
    	Joueur.initNbrJoueurs();
        for(int i = 0;i<nbJH+nbJIA;i++)
        {
            if(i<nbJH)
            listeJoueurs.add(new JoueurHumain());
            else //if(i<=nbrJoueursH)
            listeJoueurs.add(new JoueurIA());
        }
    }
        /**	Initialise la partie en param�trant le nb de joueurs humains et IA, et en cr�ant le tableau de joueurs. 
     *	Pour une partie rapide, on met le nb de graines � 2
     *	Pour une partie avanc�e, on demande au joueur s'il veut 2 graines ou 1 carte alli�*/
    public void recupCartes() {
        for(Joueur j : listeJoueurs){
            this.listeCarteIng.addAll(j.rendreCarteIng());
        }    
        
        
    }
    protected void creerCartes() {
        int[][] lune1Effet      = {{1, 1, 1, 1}, {2, 0, 1, 1}, {2, 0, 2, 0}};
        int[][] lune2Effet      = {{2, 0, 1, 1}, {1, 3, 0, 0}, {0, 1, 2, 1}};
        int[][] lune3Effet      = {{0, 0, 4, 0}, {0, 2, 2, 0}, {0, 0, 1, 3}};
        int[][] sirene1Effet    = {{1, 3, 1, 0}, {1, 2, 1, 1}, {1, 0, 4, 0}};
        int[][] sirene2Effet    = {{2, 1, 1, 1}, {1, 0, 2, 2}, {3, 0, 0, 2}};
        int[][] sirene3Effet    = {{1, 2, 2, 0}, {1, 1, 2, 1}, {2, 0, 1, 2}};
        int[][] dryade1Effet    = {{2, 1, 1, 2}, {1, 1, 1, 3}, {2, 0, 2, 2}};
        int[][] dryade2Effet    = {{0, 3, 0, 3}, {2, 1, 3, 0}, {1, 1, 3, 1}};
        int[][] dryade3Effet    = {{1, 2, 1, 2}, {1, 0, 1, 4}, {2, 4, 0, 0}};
        int[][] fontaine1Effet  = {{1, 3, 1, 2}, {2, 1, 2, 2}, {0, 0, 3, 4}};
        int[][] fontaine2Effet  = {{2, 2, 0, 3}, {1, 1, 4, 1}, {1, 2, 1, 3}};
        int[][] fontaine3Effet  = {{2, 2, 3, 1}, {2, 3, 0, 3}, {1, 1, 3, 3}};
        int[][] or1Effet        = {{2, 2, 3, 1}, {2, 3, 0, 3}, {1, 1, 3, 3}};
        int[][] or2Effet        = {{2, 2, 2, 2}, {0, 4, 4, 0}, {1, 3, 2, 2}};
        int[][] or3Effet        = {{3, 1, 3, 1}, {1, 4, 2, 1}, {2, 4, 1, 1}};
        int[][] arcEnCiel1Effet = {{4, 1, 1, 1}, {1, 2, 1, 3}, {1, 2, 2, 2}};
        int[][] arcEnCiel2Effet = {{2, 3, 2, 0}, {0, 4, 3, 0}, {2, 1, 1, 3}};
        int[][] arcEnCiel3Effet = {{2, 2, 3, 0}, {1, 1, 1, 4}, {2, 0, 3, 2}};
        int[][] dolmen1Effet    = {{3, 1, 4, 1}, {2, 1, 3, 3}, {2, 3, 2, 2}};
        int[][] dolmen2Effet    = {{2, 4, 1, 2}, {2, 2, 2, 3}, {1, 4, 3, 1}};
        int[][] dolmen3Effet    = {{3, 3, 3, 0}, {1, 3, 3, 2}, {2, 3, 1, 3}};
        int[][] fee1Effet       = {{1, 2, 2, 1}, {1, 2, 3, 0}, {0, 2, 2, 2}};
        int[][] fee2Effet       = {{4, 0, 1, 1}, {1, 1, 3, 1}, {0, 0, 3, 3}};
        int[][] fee3Effet       = {{2, 0, 3, 1}, {0, 3, 0, 3}, {1, 2, 2, 1}};
        
        listeCarteIng.add(new CarteIngredient(lune1Effet));
        listeCarteIng.add(new CarteIngredient(lune2Effet));
        listeCarteIng.add(new CarteIngredient(lune3Effet));
        listeCarteIng.add(new CarteIngredient(sirene1Effet));
        listeCarteIng.add(new CarteIngredient(sirene2Effet));
        listeCarteIng.add(new CarteIngredient(sirene3Effet));
        listeCarteIng.add(new CarteIngredient(dryade1Effet));
        listeCarteIng.add(new CarteIngredient(dryade2Effet));
        listeCarteIng.add(new CarteIngredient(dryade3Effet));
        listeCarteIng.add(new CarteIngredient(fontaine1Effet));
        listeCarteIng.add(new CarteIngredient(fontaine2Effet));
        listeCarteIng.add(new CarteIngredient(fontaine3Effet));
        listeCarteIng.add(new CarteIngredient(or1Effet));
        listeCarteIng.add(new CarteIngredient(or2Effet));
        listeCarteIng.add(new CarteIngredient(or3Effet));
        listeCarteIng.add(new CarteIngredient(arcEnCiel1Effet));
        listeCarteIng.add(new CarteIngredient(arcEnCiel2Effet));
        listeCarteIng.add(new CarteIngredient(arcEnCiel3Effet));
        listeCarteIng.add(new CarteIngredient(dolmen1Effet));
        listeCarteIng.add(new CarteIngredient(dolmen2Effet));
        listeCarteIng.add(new CarteIngredient(dolmen3Effet));
        listeCarteIng.add(new CarteIngredient(fee1Effet));
        listeCarteIng.add(new CarteIngredient(fee2Effet));
        listeCarteIng.add(new CarteIngredient(fee3Effet));
    }
}    
    
    
