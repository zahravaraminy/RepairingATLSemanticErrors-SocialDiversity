package func;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

import model.RunConfig;
import model.SolutionSet;
import utils.ToolBox;

public class ResultsCollectorRQ1 {
	
	public static String MTname = "CD2RD";//PNML2PN CD2RD UML2BPMN
	public static String RQ = "rq1";//longruns rq1
	

	//static	String dataFolderName = "R://svn/Models-Varaminy2019-data/"+MTname+"/";
	//static	String sourceFolderName = "R://svn/Models-Varaminy2019-data/"+MTname+"/Mutants";
	//public static	String dataFolderName ="E://Models-Varaminy2019-data/"+MTname+"/";
	public static	String dataFolderName ="outputfolder/resultformodeltransformation1/";
	static	String sourceFolderName = "E://Models-Varaminy2019-data/"+MTname+"/Mutants";
	static File originalMT;
	
	//public static void main(String[] args) {
	public static void extractbestsolution() {
		ToolBox.initializeRandom();
		
		originalMT = new File(sourceFolderName + "/" + GenerateMutants.ORIGINAL_TRANSFORMATION_FILE_NAME);
		
		String rq1FolderName = dataFolderName+"/"+RQ;
		
		File dataFolder = new File(dataFolderName);
		if(! dataFolder.exists() || !dataFolder.isDirectory())
			throw new IllegalStateException("Invalid folder: "+dataFolder.getAbsolutePath());
		System.out.println("Folder: "+dataFolder.getAbsolutePath());

		
		File rq1Folder = new File(rq1FolderName);
		if(! rq1Folder.exists() || !rq1Folder.isDirectory())
			throw new IllegalStateException("Invalid rq1 folder: "+rq1Folder.getAbsolutePath());
		
		File[] mutantsFolders = rq1Folder.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.isDirectory();
			}
		});
		Arrays.sort(mutantsFolders, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				String o1Number = o1.getName().substring(0,  o1.getName().indexOf("m"));
				String o2Number = o2.getName().substring(0,  o2.getName().indexOf("m"));
				return o1Number.compareTo(o2Number);
			}
		});
		if( mutantsFolders == null || mutantsFolders.length <= 0)
			throw new IllegalStateException("No mutant folders found in '"+rq1Folder.getAbsolutePath()+"'");

		System.out.println("  -> "+mutantsFolders.length+" mutants folders.");
		for (File file : mutantsFolders) {
			System.out.println(file.getAbsolutePath()+": "+file.listFiles(new FileFilter() {
				@Override
				public boolean accept(File f) {
					return f.isDirectory();
				}
			}).length + "cases");
		}
		
//		System.out.println(Arrays.toString(mutantsFolders).replaceAll(", ", "\n "));
		
		
		
		System.out.println();
		
		/*
		 * Collect and parse solutions
		 */
		HashMap<String, ArrayList<SolutionSet>> solutionsConfigurations = new HashMap<>();
		for (File mutantsFolder : mutantsFolders) {
				//\1\results\nsga\solution
			System.out.println("Mutants folder: "+mutantsFolder.getAbsolutePath());
			ArrayList<SolutionSet> solSets = collectRQ1Solutions(solutionsConfigurations, mutantsFolder);
			System.out.println("   NSGA."+Arrays.toString(computeAvgFitness(solSets)));
			System.out.println();
		}

		System.out.println("\n\nSolutions:");
		System.out.println(format(solutionsConfigurations));
		
		
		ArrayList<String> configList = new ArrayList<>(solutionsConfigurations.keySet());
		configList.sort(new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String[] o1t = o1.split("\\.");
				String[] o2t = o2.split("\\.");
				int cmp = o1t[0].compareTo(o2t[0]);
				if(cmp == 0)
					cmp = o1t[1].compareTo(o2t[1]);
				return cmp;
			}
		});
		
		
		
		String csv = SolutionSet.CSV_HEADER;
		for (File mutantsFolder : mutantsFolders) {
			System.out.println(" - ResultsCollectorRQ1.main()"+mutantsFolder.getAbsolutePath());
			String numberOfMutants = mutantsFolder.getName().substring(0,  mutantsFolder.getName().indexOf("m"));
			
			generateDIFFBetweenBestAndOriginal(solutionsConfigurations, RunConfig.NSGA+"."+numberOfMutants);
			generateDIFFBetweenBestAndMutant(solutionsConfigurations, 	RunConfig.NSGA+"."+numberOfMutants);
			csv += getCSVLines(solutionsConfigurations, 				RunConfig.NSGA+"."+numberOfMutants);
		}
		
		try {
			File out = new File(rq1Folder.getAbsolutePath()+"/"+RQ+".csv");
			ToolBox.write(csv, out);
			System.out.println("Results writen in '"+out.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//\data\experiment\rq0\4mutants\results\nsga\solution1
	
		
//		ToolBox.printLOC();
	}

	private static void generateDIFFBetweenBestAndMutant(HashMap<String, ArrayList<SolutionSet>> solutionsConfigurations, String config) {
		ArrayList<SolutionSet> solSets = solutionsConfigurations.get(config);

		if(solSets == null || solSets.size() == 0) {
			System.err.println("Configuration '"+config+"' contains no solutions.\nPossible configs are "+solutionsConfigurations.keySet());
			return;
		}

		solSets.sort(new Comparator<SolutionSet>() {
			@Override
			public int compare(SolutionSet o1, SolutionSet o2) {
				return Integer.compare(o1.getExperimentNumber(), o2.getExperimentNumber());
			}
		});


		for (SolutionSet solSet : solSets)
			try {
				solSet.generateDIFFBetweenBestAndMutant();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static void generateDIFFBetweenBestAndOriginal(HashMap<String, ArrayList<SolutionSet>> solutionsConfigurations, String config) {
		ArrayList<SolutionSet> solSets = solutionsConfigurations.get(config);
		
		if(solSets == null || solSets.size() == 0) {
			System.err.println("Configuration '"+config+"' contains no solutions.\nPossible configs are "+solutionsConfigurations.keySet());
			return;
		}
		
		solSets.sort(new Comparator<SolutionSet>() {
			@Override
			public int compare(SolutionSet o1, SolutionSet o2) {
				return Integer.compare(o1.getExperimentNumber(), o2.getExperimentNumber());
			}
		});


		for (SolutionSet solSet : solSets)
			try {
				solSet.generateDIFFBetweenBestAndOriginal(originalMT);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private static String getCSVLines(HashMap<String, ArrayList<SolutionSet>> solutionsConfigurations, String config) {
		String csv = "";
		ArrayList<SolutionSet> solSets = solutionsConfigurations.get(config);
		
		if(solSets == null || solSets.size() == 0) {
			System.err.println("Configuration '"+config+"' contains no solutions.\nPossible configs are "+solutionsConfigurations.keySet());
			return "";
		}

		solSets.sort(new Comparator<SolutionSet>() {
			@Override
			public int compare(SolutionSet o1, SolutionSet o2) {
				return Integer.compare(o1.getExperimentNumber(), o2.getExperimentNumber());
			}
		});

		

		for (SolutionSet solSet : solSets) 
			csv += solSet.getCSVLinesMinErrors();
		return csv;
	}

	private static  double[] computeAvgFitness(ArrayList<SolutionSet> solSets) {
		double[] res = new double[3];
		int numberOfSols = 0;
		for (SolutionSet s : solSets) {
			for (int i = 0; i < res.length; i++) {
				res[i] += (s.getAvgFitness()[i]);// * s.getSolutions().size());
				
			}
			numberOfSols += 1 ; //s.getSolutions().size();
		}

		for (int i = 0; i < res.length; i++)
			res[i] = res[i] / numberOfSols;
		return res;
	}

	private static ArrayList<SolutionSet> collectRQ1Solutions(HashMap<String, ArrayList<SolutionSet>> solutions, File folder) {
		//\1\results\nsga\solution
		ArrayList<SolutionSet> res = new ArrayList<>();
		File[] nsgaSolutionFolders = folder.listFiles();
		if (nsgaSolutionFolders == null) {
			System.out.println(folder.getAbsolutePath() + ": NO mutants");
		} else {
			File[] nsgaSolutions = folder.listFiles();
			for (File nsgaSol : nsgaSolutions) {//   /1/
				File f = new File(nsgaSol.getAbsolutePath()+"/results/nsga/solution/");
				if(f.exists() && f.isDirectory()) {
					SolutionSet nsgaSet = new SolutionSet(f);
					if (nsgaSet.getSolutions().size() > 0) {
						if (solutions.get(nsgaSet.getConfiguration()) == null)
							solutions.put(nsgaSet.getConfiguration(), new ArrayList<>());
						solutions.get(nsgaSet.getConfiguration()).add(nsgaSet);
						res.add(nsgaSet);
					}
				} else {
					System.out.println(f.getAbsolutePath()+" not found.");
				}
			}
		}
		return res;
	}

	static String format(Collection<SolutionSet> c) {
	  String s = c.stream().map(SolutionSet::toStringSimple).collect(Collectors.joining(","));
	  return String.format("[%s]"+c.size(), s);
	}
	
	static String format(HashMap<String, ArrayList<SolutionSet>> css) {
		String res = "";
		
		for (String keyConfig : css.keySet()) {
			res+= keyConfig +"\n";
			res += "  "+format(css.get(keyConfig)) +"\n";
		}
		
	  return String.format("[%s]", res);
	}

	static String formatAvgs(HashMap<String, double[]> avgs) {
		
		String res = "";
		
		for (String keyConfig : avgs.keySet()) {
			res+= keyConfig +"\n";
			res += "  "+Arrays.toString(avgs.get(keyConfig)) +"\n";
		}
		
	  return String.format("[%s]", res);
	}

	
}
