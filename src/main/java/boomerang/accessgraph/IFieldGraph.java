package boomerang.accessgraph;

import java.util.Collection;
import java.util.Set;

public interface IFieldGraph {
	Set<IFieldGraph> popFirstField();
	Set<IFieldGraph> popLastField();
	Collection<WrappedSootField> getEntryNode();
	WrappedSootField[] getFields();
	IFieldGraph appendFields(WrappedSootField[] toAppend);
	IFieldGraph append(IFieldGraph graph);
	IFieldGraph prependField(WrappedSootField f);
	Collection<WrappedSootField> getExitNode();
	boolean shouldOverApproximate();
	IFieldGraph overapproximation();
	IFieldGraph noType();
}
