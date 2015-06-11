package mmlib4j.imagej.guj;
import ij.gui.Plot;
import ij.util.Tools;

import java.awt.Color;
import java.util.ArrayList;

import mmlib4j.representation.tree.IMorphologicalTreeFiltering;
import mmlib4j.representation.tree.InfoPrunedTree;
import mmlib4j.representation.tree.componentTree.ComponentTree;
import mmlib4j.representation.tree.componentTree.NodeCT;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueCT;
import mmlib4j.representation.tree.pruningStrategy.ComputerExtinctionValueToS;
import mmlib4j.representation.tree.pruningStrategy.ComputerMserCT;
import mmlib4j.representation.tree.pruningStrategy.ComputerMserToS;
import mmlib4j.representation.tree.pruningStrategy.PruningBasedGradualTransition;
import mmlib4j.representation.tree.tos.NodeToS;
import mmlib4j.representation.tree.tos.TreeOfShape;

public class HistogramOfBranch {

	private int indexAttr;
	int x;
	int y;
	
	public HistogramOfBranch (int indexAttr, int x, int y) {
		this.indexAttr = indexAttr;
		this.x = x;
		this.y = y;
	}
	
	public void showScoreMser(NodeCT node, boolean[] selected, Double []q) {
		int contStable = 0;
		int contMser = 0;
		for (int i = 0; i < q.length; i++) {
			if(q[i] != null){
				contStable++;
			}
			if(selected[i] && q[i] != null){
				contMser++;
			}
		}
		
		
		double score[] = new double[contStable];
		double indiceScore[] = new double[contStable];
		double indiceMser[] = new double[contMser];
		double scoreMser[] = new double[contMser];
		int contScore = 0;
		int contScoreMser = 0;
		for (int i = 0; i < q.length; i++) {
			if(q[i] != null){
				indiceScore[contScore] = i;
				score[contScore] = q[i];
				contScore++;
			}
			if(selected[i] && q[i] != null){
				indiceMser[contScoreMser] = i;
				scoreMser[contScoreMser] = q[i];
				contScoreMser++;
			}
			
		}

		Plot pw = new Plot("Stability function for the nodes of the path (which contains selected node) from leaf to the root.", "nodes stable", "score of nodes stable", indiceScore, score);
		double[] a = Tools.getMinMax(indiceScore);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(score);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(xmin, xmax, ymin, ymax);
		//pw.addPoints(indiceScore, score, Plot.CIRCLE);
		
		pw.setColor(Color.RED);
		pw.addPoints(indiceMser, scoreMser, Plot.CIRCLE);
		pw.addPoints(indiceMser, scoreMser, Plot.BOX);
		pw.addPoints(indiceMser, scoreMser, Plot.X);
		
		
		pw.setColor(Color.BLACK);
		pw.addPoints(indiceScore, score, Plot.CROSS);
		
		pw.show();
		
	}

	public void showScoreMser(Double []q) {

		 
		int contStable = 0;
		for (int i = 0; i < q.length; i++) {
			if(q[i] != null){
				contStable++;
			}
			
		}
		
		double score[] = new double[contStable];
		double indiceScore[] = new double[contStable];
		int contScore = 0;
		for (int i = 0; i < q.length; i++) {
			if(q[i] != null){
				indiceScore[contScore] = i;
				score[contScore] = q[i];
				contScore++;
			}
		}

		Plot pw = new Plot("Function of stability", "nodes stable", "score of nodes stable", indiceScore, score);
		double[] a = Tools.getMinMax(indiceScore);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(score);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(xmin, xmax, ymin, ymax);
		pw.addPoints(indiceScore, score, Plot.CIRCLE);
		pw.show();
		
	}
	
	public void run(InfoPrunedTree prunedTree, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		IMorphologicalTreeFiltering treeIn = prunedTree.getTree();
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			if (typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE){
				ComputerExtinctionValueCT ev = new ComputerExtinctionValueCT(tree);
				boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr, prunedTree);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}else if(typePruning == IMorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserCT mser = new ComputerMserCT(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta, prunedTree); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				showScoreMser(mser.getScore());
				showScoreMser(node, selected, mser.getScoreOfBranch(node));
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}
			else if(typePruning == IMorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(treeIn, prunedTree.getAttributeType(), delta);
				boolean selected[] = gt.getMappingSelectedNodes(prunedTree); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));	
				}
			}
			
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			
			if (typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE){
				ComputerExtinctionValueToS ev = new ComputerExtinctionValueToS(tree);
				boolean selected[] = ev.getExtinctionValueNode(indexAttr, prunedTree);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				
				
			}
			else if(typePruning == IMorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserToS mser = new ComputerMserToS(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta, prunedTree); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					selected2[node.hashCode()] = true;
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
					if(selected[node.hashCode()]){
						listEVPath.add(node);
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				while(prunedTree.wasPruned(node)){
					node = node.getParent();
				}
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					node = node.getParent();
					listPx.add(new Float(node.getLevel()));
					listPy.add(getAttribute(node));
				}
				
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Histogram", "level", "attribute", vPx, vPy);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(xmin-5, xmax+5, ymin-5, ymax-5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPxEV, vPyEV, Plot.CIRCLE);
			pw.addPoints(vPxEV, vPyEV, Plot.BOX);
			pw.addPoints(vPxEV, vPyEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPx, vPy, Plot.CROSS);
		pw.show();
	}
	

	public void run(IMorphologicalTreeFiltering treeIn, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			if (typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE){
				ComputerExtinctionValueCT ev = new ComputerExtinctionValueCT(tree);
				boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}else if(typePruning == IMorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserCT mser = new ComputerMserCT(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				//showScoreMser(mser.getScore());
				showScoreMser(node, selected, mser.getScoreOfBranch(node));
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}
			else if(typePruning == IMorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(treeIn, indexAttr, delta);
				boolean selected[] = gt.getMappingSelectedNodes(); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();	
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
					}else{
						node = node.getParent();
					}
				}
			}
			
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			
			if (typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE){
				ComputerExtinctionValueToS ev = new ComputerExtinctionValueToS(tree);
				boolean selected[] = ev.getExtinctionValueNode(indexAttr, delta);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
				
				
			}
			else if(typePruning == IMorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserToS mser = new ComputerMserToS(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(getAttribute(n));
				}
			}
			else{
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();	
						listPx.add(new Float(node.getLevel()));
						listPy.add(getAttribute(node));
					}else
						node = node.getParent();
				}
				
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Residual evolution for the pixel (" + this.x + ", " + this.y + ")", "attribute value", "level", vPy, vPx);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(ymin-5, ymax-5, xmin-5, xmax+5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPyEV, vPxEV, Plot.CIRCLE);
			pw.addPoints(vPyEV, vPxEV, Plot.BOX);
			pw.addPoints(vPyEV, vPxEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPy, vPx, Plot.CROSS);
		pw.show();
	}
	

	public void runPrimitivesFamily(IMorphologicalTreeFiltering treeIn, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxSelected = null;
		ArrayList<Float> listPySelected = null;
		
		
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			if (typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE){
				ComputerExtinctionValueCT ev = new ComputerExtinctionValueCT(tree);
				boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr);
				boolean selected2[] = new boolean[tree.getNumNode()];
				
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}else if(typePruning == IMorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserCT mser = new ComputerMserCT(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				//showScoreMser(mser.getScore());
				showScoreMser(node, selected, mser.getScoreOfBranch(node));
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
				//VisualizationComponentTree.getInstance(prunedTree, selected, selected2).setVisible(true);
			}
			else if(typePruning == IMorphologicalTreeFiltering.PRUNING_GRADUAL_TRANSITION){
				PruningBasedGradualTransition gt = new PruningBasedGradualTransition(treeIn, indexAttr, delta);
				boolean selected[] = gt.getMappingSelectedNodes(); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				selected2[node.hashCode()] = true;
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();	
					}
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeCT n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
			}
			else{
				
				NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
					}else{
						node = node.getParent();
					}
				}
			}
			
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			
			if (typePruning == IMorphologicalTreeFiltering.EXTINCTION_VALUE){
				ComputerExtinctionValueToS ev = new ComputerExtinctionValueToS(tree);
				boolean selected[] = ev.getExtinctionValueNode(indexAttr, delta);
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
				
				
			}
			else if(typePruning == IMorphologicalTreeFiltering.PRUNING_MSER){
				ComputerMserToS mser = new ComputerMserToS(tree);
				boolean selected[] = mser.getMappingNodesByMSER(delta); 
				boolean selected2[] = new boolean[tree.getNumNode()];
				ArrayList<NodeToS> listEVPath = new ArrayList<NodeToS>();
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				selected2[node.hashCode()] = true;
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();
						selected2[node.hashCode()] = true;
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
						if(selected[node.hashCode()]){
							listEVPath.add(node);
						}
					}else{
						node = node.getParent();
					}
					
				}
				
				listPxSelected = new ArrayList<Float>();
				listPySelected = new ArrayList<Float>();
				for(NodeToS n: listEVPath){
					listPxSelected.add(new Float(n.getLevel()));
					listPySelected.add(contNodes[n.getId()]);
				}
			}
			else{
				
				NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
				
				float contNodes[] = new float[tree.getNumNode()];
				int cont = 0;
				contNodes[node.getId()] = cont++;
				
				listPx.add(new Float(node.getLevel()));
				listPy.add(contNodes[node.getId()]);
				while (node.getParent() != null) {
					if(getAttribute(node) != getAttribute(node.getParent())){
						node = node.getParent();	
						listPx.add(new Float(node.getLevel()));
						contNodes[node.getId()] = cont++;
						listPy.add(contNodes[node.getId()]);
					}else
						node = node.getParent();
				}
				
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Residual evolution for the pixel (" + this.x + ", " + this.y + ")", "primitives family (index)", "level", vPy, vPx);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(ymin-5, ymax-5, xmin-5, xmax+5);
		
		
		if(listPxSelected != null){
			float vPxEV[] = new float[listPxSelected.size()];
			float vPyEV[] = new float[listPySelected.size()];
			for (int i = 0; i < listPxSelected.size(); i++) {
				vPxEV[i] = listPxSelected.get(i);
				vPyEV[i] = listPySelected.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPyEV, vPxEV, Plot.CIRCLE);
			pw.addPoints(vPyEV, vPxEV, Plot.BOX);
			pw.addPoints(vPyEV, vPxEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPy, vPx, Plot.CROSS);
		pw.show();
	}
	
	public void run_OLD(IMorphologicalTreeFiltering treeIn, int typePruning, int delta) {
		ArrayList<Float> listPx = new ArrayList<Float>();
		ArrayList<Float> listPy = new ArrayList<Float>();

		ArrayList<Float> listPxEV = null;
		ArrayList<Float> listPyEV = null;
		
		
		if(treeIn instanceof ComponentTree){	
			ComponentTree tree = (ComponentTree) treeIn;
			
			ComputerExtinctionValueCT ev = new ComputerExtinctionValueCT(tree);
			boolean selected[] = ev.getExtinctionValueNodeCT(indexAttr);
			boolean selected2[] = new boolean[tree.getNumNode()];
			ArrayList<NodeCT> listEVPath = new ArrayList<NodeCT>();
			
			NodeCT node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
			selected2[node.hashCode()] = true;
			listPx.add(new Float(node.getLevel()));
			listPy.add(getAttribute(node));
			while (node.getParent() != null) {
				selected2[node.hashCode()] = true;
				node = node.getParent();
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
				if(selected[node.hashCode()]){
					listEVPath.add(node);
				}
				
			}
			
			listPxEV = new ArrayList<Float>();
			listPyEV = new ArrayList<Float>();
			for(NodeCT n: listEVPath){
				listPxEV.add(new Float(n.getLevel()));
				listPyEV.add(getAttribute(n));
			}
			
			//VisualizationComponentTree.getInstance(tree).setVisible(true);
			
		}else{
			TreeOfShape tree = (TreeOfShape) treeIn;
			NodeToS node = tree.getSC(y * treeIn.getInputImage().getWidth() + x);
			listPx.add(new Float(node.getLevel()));
			listPy.add(getAttribute(node));
			while (node.getParent() != null) {
				node = node.getParent();
				listPx.add(new Float(node.getLevel()));
				listPy.add(getAttribute(node));
			}
		}
		
		float vPx[] = new float[listPx.size()];
		float vPy[] = new float[listPy.size()];
		for (int i = 0; i < listPx.size(); i++) {
			vPx[i] = listPx.get(i);
			vPy[i] = listPy.get(i);
		}

		Plot pw = new Plot("Histogram", "attribute","level", vPy, vPx);
		double[] a = Tools.getMinMax(vPx);
		double xmin = a[0], xmax = a[1];
		a = Tools.getMinMax(vPy);
		double ymin = a[0], ymax = a[1];
		pw.setSize(1000, 500);
		pw.setColor(Color.BLUE);
		pw.setLimits(ymin-5, ymax-5, xmin-5, xmax+5);
		
		
		if(listPxEV != null){
			float vPxEV[] = new float[listPxEV.size()];
			float vPyEV[] = new float[listPyEV.size()];
			for (int i = 0; i < listPxEV.size(); i++) {
				vPxEV[i] = listPxEV.get(i);
				vPyEV[i] = listPyEV.get(i);
				
			}
			pw.setColor(Color.RED);
			pw.addPoints(vPyEV, vPxEV, Plot.CIRCLE);
			pw.addPoints(vPyEV, vPxEV, Plot.BOX);
			pw.addPoints(vPyEV, vPxEV, Plot.X);
		}
		pw.setColor(Color.BLACK);
		pw.addPoints(vPy, vPx, Plot.CROSS);
		pw.show();
	}
	
	public boolean isFound(ArrayList<NodeCT> list, NodeCT node){
		for(NodeCT n: list){
			if(node.getId() == n.getId())
				return true;
		}
		return false;
	}

	public float getAttribute(NodeCT node){
		if(indexAttr == IMorphologicalTreeFiltering.ATTRIBUTE_AREA)
			return node.getArea();
		else if(indexAttr == IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME)
			return node.getAttributeValue(IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME);
		else if(indexAttr == IMorphologicalTreeFiltering.ATTRIBUTE_HEIGHT)
			return node.getHeightNode();
		else if(indexAttr == IMorphologicalTreeFiltering.ATTRIBUTE_WIDTH)
			return node.getWidthNode();
		else if(indexAttr == IMorphologicalTreeFiltering.ATTRIBUTE_ALTITUDE)
			return node.getLevel();
		return -1;
	}
	

	public float getAttribute(NodeToS node){
		if(indexAttr == 0)
			return node.getArea();
		else if(indexAttr == 1)
			return node.getAttributeValue(IMorphologicalTreeFiltering.ATTRIBUTE_VOLUME);
		else if(indexAttr == 2)
			return node.getHeightNode();
		else
			return node.getWidthNode();
	}
}