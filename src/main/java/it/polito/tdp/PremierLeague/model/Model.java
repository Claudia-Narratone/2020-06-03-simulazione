package it.polito.tdp.PremierLeague.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.sun.javafx.collections.MappingChange.Map;

import it.polito.tdp.PremierLeague.db.PremierLeagueDAO;

public class Model {
	
	private PremierLeagueDAO dao;
	private Graph<Player, DefaultWeightedEdge> graph;
	private HashMap<Integer,Player> idMap;
	private int bestDegree;
	private List<Player> dreamTeam;
	
	public Model() {
		dao=new PremierLeagueDAO();
	}

	public void creaGrafo(double goal) {
		graph=new SimpleDirectedWeightedGraph<Player, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		idMap=new HashMap<Integer, Player>();
		dao.getPlayers(goal, idMap);
		
		Graphs.addAllVertices(graph, idMap.values());
		
		for(Adiacenza adj : dao.getAdiacenze(idMap)) {
			if(graph.containsVertex(adj.getP1()) && graph.containsVertex(adj.getP2())) {
				if(adj.getPeso() < 0) {
					//arco da p2 a p1
					Graphs.addEdgeWithVertices(graph, adj.getP2(), adj.getP1(), ((double) -1)*adj.getPeso());
				} else if(adj.getPeso() > 0){
					//arco da p1 a p2
					Graphs.addEdgeWithVertices(graph, adj.getP1(), adj.getP2(), adj.getPeso());
				}
			}
		}
		
		System.out.println(String.format("Grafo creato con %d vertici e %d archi", graph.vertexSet().size(), graph.edgeSet().size()));
	}
	
	public TopPlayer trovaTopPlayer() {
		if(graph==null)
			return null;
		
		Player best=null;
		Integer maxDegree=Integer.MIN_VALUE;
		for (Player p:graph.vertexSet()) {
			if(graph.outDegreeOf(p)>maxDegree) {
				maxDegree=graph.outDegreeOf(p);
				best=p;
			}
		}
		
		TopPlayer topPlayer=new TopPlayer();
		topPlayer.setPlayer(best);
		
		List<Opponent> opponents= new ArrayList<Opponent>();
		for(DefaultWeightedEdge edge:graph.outgoingEdgesOf(topPlayer.getPlayer())) {
			opponents.add(new Opponent(graph.getEdgeTarget(edge), (int)graph.getEdgeWeight(edge)));
		}
		
		Collections.sort(opponents);
		topPlayer.setOpponents(opponents);
		return topPlayer;
	}

	
	public List<Player> getDreamTeam(int k) {
		this.bestDegree = 0;
		this.dreamTeam = new ArrayList<Player>();
		List<Player> partial = new ArrayList<Player>();
		
		this.recursive(partial, new ArrayList<Player>(this.graph.vertexSet()), k);

		return dreamTeam;
	}
	
	private void recursive(List<Player> parziale, List<Player> players, int k) {
		//ccondiz. terminazione
		if(parziale.size()==k) {
			int degree=getDegree(parziale);
			if (degree>bestDegree) {
				dreamTeam=new ArrayList<Player>(parziale);
				bestDegree=degree;
			}
			return;
		}
		
		for(Player p : players) {
			if(!parziale.contains(p)) {
				parziale.add(p);
				//i "battuti" di p non possono più essere considerati
				List<Player> remainingPlayers = new ArrayList<>(players);
				remainingPlayers.removeAll(Graphs.successorListOf(graph, p));
				recursive(parziale, remainingPlayers, k);
				parziale.remove(p);
				
			}
		}
	}
	
	private int getDegree(List<Player> team) {
		int degree = 0;
		int in;
		int out;

		for(Player p : team) {
			in = 0;
			out = 0;
			for(DefaultWeightedEdge edge : this.graph.incomingEdgesOf(p))
				in += (int) this.graph.getEdgeWeight(edge);
			
			for(DefaultWeightedEdge edge : graph.outgoingEdgesOf(p))
				out += (int) graph.getEdgeWeight(edge);
		
			degree += (out-in);
		}
		return degree;
	}

	public int nVertici() {
		return graph.vertexSet().size();
	}

	public int nArchi() {
		return graph.edgeSet().size();
	}

	public int getBestDegree() {
		return bestDegree;
	}
	
	
}
