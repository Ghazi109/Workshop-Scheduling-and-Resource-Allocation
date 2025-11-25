package main;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import data.*;


public class WorkshopScheduler {
   private Workshop workshop;
   private Model model;
   private Solver solver;
   private Solution mySolution;
   private int nbTasks;
   private int nbPauses;
   private int nbStations;
   private int nbWorkers;
   private int nbActivityTypes;
   private int startDay;
   private int endDay;
   private int minIndice;
   private int [] Duration;
   private int [] ActivityType;
   private  int [][]   StationsActivities;
   private int [][] StationsWorkers;
   private List<int[]>[] WorkerPauses;
   private int [] StartShiftWorker;
   private int [] EndShiftWorker;
   private LinkedList<Activity> listeactivity;
   private IntVar Z;

  
   private IntVar[] DurationV;
   private IntVar[] startTimes;
   private IntVar[] endTimes;
   private IntVar[] Stations;
   private IntVar[] workers;
  
   public WorkshopScheduler(Workshop workshop)
   {
       //workshop
   	this.workshop=workshop;


   	
  	//listeActivity
  	this.listeactivity=new LinkedList<Activity>();
  	for(Furniture F:workshop.getFurnitures()) {
  		for(Activity A:F.getActivities()) {
  			this.listeactivity.add(A);
  		}
  	}

    int minIndice=Integer.parseInt( workshop.getFurnitures()[0].getActivities()[0].getId().substring(1));
    this.minIndice=minIndice;
  	

   	  
	   //nbTasks
      int nbTasks=0;
      for (Furniture F:workshop.getFurnitures())
      {
      	nbTasks+=F.getActivities().length;
      }
      this.nbTasks=nbTasks;
      //System.out.println("Nombre de taches:"+this.nbTasks);
     
      //nbStations
      int nbStations=workshop.getStations().length;
      this.nbStations=nbStations;
      //System.out.println("Nombre de Stations:"+this.nbStations);
     
      //nbWorkers
      int nbWorkers=workshop.getWorkers().length;
      this.nbWorkers=nbWorkers;
      //System.out.println("Nombre d'ouvriers:"+this.nbWorkers);
     
      //nbActivityTypes
      int nbActivityTypes=4;
      this.nbActivityTypes=nbActivityTypes;
     
      //nbPauses
      int nbPauses=0;
      for (Worker W:workshop.getWorkers())
      {
      	nbPauses+=W.getBreaks().length;
      }
      this.nbPauses=nbPauses;
     
      //WorkerPauses (les pauses de chaque ouvrier
      List<int[]>[] WorkerPauses = new ArrayList[nbWorkers];
      for (int j = 0; j < nbWorkers; j++) {
         WorkerPauses[j] = new ArrayList<>();
      }
      int cpt=0;
      for (Worker W:workshop.getWorkers())
      {
      	for (LocalDateTime [] P:W.getBreaks())
      	{
          	int [] Pause=new int[2];
      		int heureS=P[0].getHour();
      		int minutesS=P[0].getMinute();
      		Pause[0]=60*heureS+minutesS;
      		int heureE=P[1].getHour();
      		int minutesE=P[1].getMinute();
      		Pause[1]=60*heureE+minutesE;
      		WorkerPauses[cpt].add(Pause);
      		//System.out.println(Pause[0]+" "+Pause[1]);
      	}
      	cpt+=1;
      	
      }
      this.WorkerPauses=WorkerPauses;
    
     
      //Start and End shift for workers
      int []StartShiftWorker=new int[nbWorkers];
      int []EndShiftWorker=new int[nbWorkers];
      for (int i=0;i<nbWorkers;i++)
      {
	       	LocalDateTime Start = LocalDateTime.of(2022, 3, 15, 10, 30);
	       	LocalDateTime End=  LocalDateTime.of(2022, 3, 15, 10, 30);
	       	for (Shift Sh:workshop.getShifts())
	       	{
	       		if (Sh.getId().equals( workshop.getWorkers()[i].getShift()) )
	       		{
	       			Start=Sh.getStart();
	       			End=Sh.getEnd();
	       			break;
	       		}
	       	}
	       	
	       	int heureS=Start.getHour();
	   		int minutesS=Start.getMinute();
	   		StartShiftWorker[i]=heureS*60+minutesS;
	   		//System.out.println("OUvrier "+i+":"+StartShiftWorker[i]);
	       	int heureE=End.getHour();
	   		int minutesE=End.getMinute();
	   		EndShiftWorker[i]=heureE*60+minutesE;
	   		//System.out.println("OUvrier "+i+":"+EndShiftWorker[i]);
      }
      this.StartShiftWorker=StartShiftWorker;
      this.EndShiftWorker=EndShiftWorker;
     
      //StartDay
      LocalDateTime startDay1=workshop.getShifts()[0].getStart();
      System.out.println(startDay1);
      int heure = startDay1.getHour();
      int minutes = startDay1.getMinute();
      int startDay = heure*60+minutes;
      this.startDay=startDay;
     
      //EndDay
      LocalDateTime endDay1=workshop.getShifts()[1].getEnd();
      System.out.println(endDay1);
      int heure1 = endDay1.getHour();
      int minutes1 = endDay1.getMinute();
      int endDay = heure1*60+minutes1;
      this.endDay=endDay;
     
      //Duration + ActivityType
      int Duration[]=new int[nbTasks];
      int ActivityType[]=new int[nbTasks];
      int i=0;
      for (Furniture F:workshop.getFurnitures())
      {
      	for (Activity A:F.getActivities())
      	{
      		Duration[i]=A.getDuration();
      		ActivityType[i]=ActivityTypeConverter.convertToInt(A.getType());
      		i+=1;
      	}
      }
      this.Duration=Duration;
      this.ActivityType=ActivityType;
     
      //StationsActivties (Chaque type d'activités : les stations possibles)
      List<Integer>[] Stations_Activity = new ArrayList[nbActivityTypes];
      for (int j = 0; j < nbActivityTypes; j++) {
          Stations_Activity[j] = new ArrayList<>();
      }
      for (int j=0;j<nbStations;j++)
      {
      	for (ActivityType A:workshop.getStations()[j].getActivityTypes() )
      	{
      		int k=ActivityTypeConverter.convertToInt(A);
      		Stations_Activity[k].add(j);
      	}
      }
      int [][]StationsActivities= new int[nbActivityTypes][];
      for (int j=0;j<nbActivityTypes;j++)
      {
      	StationsActivities[j]=Stations_Activity[j].stream().mapToInt(Integer::intValue).toArray();
      }
      this.StationsActivities=StationsActivities;
     
     
      //StationsWorkers (Chaque ouvrier les stations sur lesquelles il peut travailler)
      List<Integer>[] Stations_Worker = new ArrayList[nbWorkers];
      for (int j = 0; j < nbWorkers; j++) {
          Stations_Worker[j] = new ArrayList<>();
      }
      for (int j=0;j<nbWorkers;j++)
      {
      	for (String S:workshop.getWorkers()[j].getStations())
      	{
      		Stations_Worker[j].add(Integer.parseInt(S.substring(1)));
      	}
      }
      int [][] StationsWorkers=new int[nbWorkers][];
      for (int j=0;j<nbWorkers;j++)
      {
      	StationsWorkers[j]=Stations_Worker[j].stream().mapToInt(Integer::intValue).toArray();
      }
      this.StationsWorkers=StationsWorkers;
     
  }
  
   public void buildModel()
   {
	   model = new Model();
   	   solver = model.getSolver();
       startTimes = model.intVarArray("Début", nbTasks, startDay,endDay);
       endTimes=model.intVarArray("Fin", nbTasks,startDay,endDay);
       DurationV=model.intVarArray("Durée",nbTasks,0,endDay-startDay);
       Stations=model.intVarArray("Stations", nbTasks,0,nbStations-1);
       workers=model.intVarArray("Workers",nbTasks,0,nbWorkers-1);
       Z=model.intVar(0,100000);

    }
  
//contraintes




   public void precedence()
   {
       for (Furniture F:workshop.getFurnitures())
       {
       	for (Activity[] P:F.getPrecedence())
       	{
       		int A1=Integer.parseInt( P[0].getId().substring(1))-minIndice;
       		int A2=Integer.parseInt( P[1].getId().substring(1))-minIndice;
       		model.arithm(startTimes[A2],"-",startTimes[A1],">=",Duration[A1]).post();
       		
       		
       	}
       }
   	
   }
  
  
   public void sequence()
   {
   	for (Furniture F:workshop.getFurnitures())
   	{
   		for (int i=0;i<F.getSequences().length;i++)
   		{
   			for (int j=i+1;j<F.getSequences().length;j++)
   			{
   				for ( Activity A1:F.getSequences()[i])
   				{
   					for (Activity A2 :F.getSequences()[j])
   					{
   						int N1=Integer.parseInt( A1.getId().substring(1))-minIndice;
   						int N2=Integer.parseInt( A2.getId().substring(1))-minIndice;
   		        		model.arithm(startTimes[N2],"-",startTimes[N1],">=",Duration[N1]).post();
   					}
   				}
   				
   			}
   			
   		}
   				
   	}
   }
  
  
   public void Station_correpond_au_type_dactivité()
   {
   	//la variable station[i] appartient à l'ensemble des stations ou on peut faire l'activité de la tache i
   	for (int i=0;i<nbTasks;i++)
   	{
   		model.member(Stations[i],StationsActivities[ActivityType[i]]).post();
   		
   	}
   }
  
  
   public void Ouvrier_peut_travailler_sur_la_station_affectée()
   {
   	for (int i=0;i<nbTasks;i++)
   	{
   		for (int j=0;j<nbWorkers;j++)
   		{
   			model.ifThen(model.arithm(workers[i],"=",j),model.member(Stations[i],StationsWorkers[j]));
   		}
   	}
  }
  
  
   public void Ouvrier_fait_une_tache_à_la_fois()
   {
       IntVar[] heights = new IntVar[nbTasks];
       Task[] tasks = new Task[nbTasks];
		for (int j = 0; j < nbWorkers; j++)
		{
			for (int i=0 ;i<nbTasks ;i++)
			{
	            heights[i] = model.intVar("height_" + i, 0,1);
				model.ifThenElse(model.arithm(workers[i], "=",j),model.arithm(heights[i], "=",1),model.arithm(heights[i],"=",0));
				tasks[i] = new Task(startTimes[i], DurationV[i],endTimes[i]);
			}
			IntVar capacity = model.intVar(1);
			model.cumulative(tasks, heights, capacity).post();
	     }
   }


  
   public void Une_seule_tache_par_station_a_la_fois()
   {
       IntVar[] heights = new IntVar[nbTasks];
       Task[] tasks = new Task[nbTasks];
		for (int j = 0; j < nbStations; j++)
		{
			for (int i=0 ;i<nbTasks ;i++)
			{
	            heights[i] = model.intVar("height_" + i, 0,1);
				model.ifThenElse(model.arithm(Stations[i], "=",j),model.arithm(heights[i], "=",1),model.arithm(heights[i],"=",0));
				tasks[i] = new Task(startTimes[i], DurationV[i],endTimes[i]);
			}
			IntVar capacity = model.intVar(1);
			model.cumulative(tasks, heights, capacity).post();
	     }
   }


  
   public void initialiser_temps_deb_avec_shifts()
   {
   	for (int i=0;i<nbTasks;i++)
   	{
   		for (int j=0;j<nbWorkers;j++)
   		{
   			model.ifThen(model.arithm(workers[i],"=",j),model.arithm(startTimes[i],">=",StartShiftWorker[j]));
   			model.ifThen(model.arithm(workers[i],"=",j),model.arithm(endTimes[i],"<=",EndShiftWorker[j]));
   			
   		}
   	}
   }
  
  
   public void Ne_pas_commencer_pendant_une_pause()
   {
   	for (int i=0;i<nbTasks;i++)
   	{
   		for (int j=0;j<nbWorkers;j++)
   		{
   			for (int[] Pause:WorkerPauses[j])
   			{
   				model.ifThen(model.arithm(workers[i],"=",j),model.or(model.arithm(startTimes[i],"<",Pause[0]),model.arithm(startTimes[i],">=",Pause[1])));
   			}
   		}
   	}
   }


   public void initialiser_DurationV()
   {
   	for (int i=0;i<nbTasks;i++)
   	{
   		model.arithm(DurationV[i],"=",Duration[i]).post();


   	}
   }


  
   public void En_cas_de_supperposition_Augmenter_la_durée()
   {
   	for (int i=0;i<nbTasks;i++)
   	{
   		for (int j=0;j<nbWorkers;j++)
   		{
   			for (int[] Pause:WorkerPauses[j])
   			{
       			model.ifThen(model.and(model.arithm(startTimes[i],">",Pause[0]-Duration[i]),model.arithm(workers[i],"=",j)),model.arithm(DurationV[i],"=",Duration[i]+Pause[1]-Pause[0]));
   			}
   		}
   	}
   }
   
   public void calcul_Z()
   {
   		model.sum(DurationV,"=", Z).post();
   	
   }
   
   public void Ne_pas_interrompre_les_taches()
   {
   	for (int i=0;i<nbTasks;i++)
   	{
   		for (int j=0;j<nbWorkers;j++)
   		{
   			for (int[] Pause : WorkerPauses[j])
   			{
   				model.ifThen(model.arithm(workers[i], "=", j), model.or(model.arithm(startTimes[i],">",Pause[1]),model.arithm(startTimes[i],"<=",Pause[0]-Duration[i])));
   			}
   		}
   	}
   }

  
  
   public void addConstraints()
   {
   	precedence();
   	sequence();
   	Station_correpond_au_type_dactivité();
   	Ouvrier_peut_travailler_sur_la_station_affectée();
   	Ouvrier_fait_une_tache_à_la_fois();
   	Une_seule_tache_par_station_a_la_fois();
   	initialiser_temps_deb_avec_shifts();
   	Ne_pas_commencer_pendant_une_pause();
    //initialiser_DurationV();
   	//En_cas_de_supperposition_Augmenter_la_durée();
   	Ne_pas_interrompre_les_taches();
   	//calcul_Z();
   }
   
   public void solve2()
   {
		mySolution= solver.findSolution();
  	 	System.out.println(solver.getSolutionCount() + " solutions found.");
	   
   }
   public void solve1()
   {
		mySolution= solver.findOptimalSolution(Z,false);
  	 	System.out.println(solver.getSolutionCount() + " solutions found.");
   }
  
  public void afficher_solution()
  {
  	if (mySolution!=null)
  	{
      	for (int i=0;i<nbTasks;i++)
      	{
      		StringBuilder sb = new StringBuilder();
      		sb.append("Tache : ");
      		sb.append(this.listeactivity.get(i).getId());
      		sb.append(" de type : ");
      		sb.append(this.listeactivity.get(i).getType());
      		sb.append(", de durée : ");
      		sb.append(this.listeactivity.get(i).getDuration());
      		sb.append(", Commence à : ");
      		sb.append(mySolution.getIntVal(startTimes[i]));
      		sb.append(", Finie à : ");
      		sb.append(mySolution.getIntVal(endTimes[i]));
      		sb.append(", effectuée par l'ouvrier : ");
      		sb.append(mySolution.getIntVal(workers[i]));
      		sb.append(", sur la station : ");
      		sb.append(mySolution.getIntVal(Stations[i]));
      		sb.toString();
      		System.out.println(sb);
      	}
  		
  	}
  }


 
  
	
	
	
	
   public static void main(String[] args) throws Exception {
       long time = System.currentTimeMillis();
       Workshop workshop = Utils.fromFile("data/workshop_0.json", Workshop.class);
       WorkshopScheduler WS=new WorkshopScheduler(workshop);
       WS.buildModel();
       WS.addConstraints();
       WS.solve2();
       WS.afficher_solution();
       List<Integer>[] TasksByWorker = new ArrayList[WS.nbWorkers];
       for (int j = 0; j < WS.nbWorkers; j++) {
       	TasksByWorker[j] = new ArrayList<>();
       }
       
       for (int j=0 ;j<WS.nbWorkers;j++)
       {
       	for (int i=0;i<WS.nbTasks;i++)
       	{
       		if (WS.workers[i].getValue()==j)
       		{
       			TasksByWorker[j].add(i);
       		}
       	}
       }
       int min=1000000;
       for (int i=0;i<WS.nbTasks;i++)
       {
       	if (WS.startTimes[i].getValue()<min)
       		min=WS.startTimes[i].getValue();

       }
       System.out.println(min);
       ScheduleVisualization.displaySchedule(TasksByWorker, WS.DurationV, WS.startTimes,min-10,WS.WorkerPauses);
       
       List<Integer>[] TasksByStation = new ArrayList[WS.nbStations];
       for (int j = 0; j < WS.nbStations; j++) {
       	TasksByStation[j] = new ArrayList<>();
       }
       
       for (int j=0 ;j<WS.nbStations;j++)
       {
       	for (int i=0;i<WS.nbTasks;i++)
       	{
       		if (WS.Stations[i].getValue()==j)
       		{
       			TasksByStation[j].add(i);
       		}
       	}
       }
       for (int i=0;i<WS.nbTasks;i++)
       {
       	if (WS.startTimes[i].getValue()<min)
       		min=WS.startTimes[i].getValue();

       }
       System.out.println(min);
       ScheduleVisualization2.displaySchedule(TasksByStation, WS.DurationV, WS.startTimes,min-10);
       
       
      
    }
      
    }
   
