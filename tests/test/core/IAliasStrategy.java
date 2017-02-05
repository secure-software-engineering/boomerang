package test.core;

import com.google.common.collect.Multimap;

import boomerang.accessgraph.AccessGraph;
import soot.Local;
import soot.Unit;
import soot.jimple.spark.pag.Node;
import soot.util.Chain;
import soot.util.MultiMap;


public interface IAliasStrategy<Fact, Results> {
  Fact parseFact(String method, Chain<Local> locals, String arg);

  void setup();

  void beforeAll();

  Results query(Fact fact, Unit stmt, boolean allContexts);

  Multimap<Unit, Fact> makeComparable(Results res);

  void afterSootInit();

  Local getLocal(Fact fact);

  boolean isLocal(Fact fact);

  void compareToSpark(Results res, MultiMap<Node, AccessGraph> sparkResults);
}
