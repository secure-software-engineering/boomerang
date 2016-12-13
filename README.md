# Boomerang

Boomerang is a demand-driven context and flow-sensitive pointer analysis for Java. It is built on top of [Soot](https://sable.github.io/soot/) and [Heros](https://github.com/Sable/heros). 

The key novelties of Boomerang are the *query format* and the *client-driven context-resolution*.

*Query format:* Boomerang delivers rich query information: For a given variable and statement, Boomerang returns the points-to set and additionally **all** pointers visible in the current method that point to the allocation sites in the points-to set. Pointers in Boomerang are abstracted as [access graphs](src/boomerang/accessgraph/AccessGraph.java). Access graphs are used to support field accesses.

*Client-driven context-resolution:* Each query of Boomerang can be limited to a context dictated by the client - the analysis Boomerang is integrated into. Instead of merging points-to information across all possible call sites of a method, Boomerang allows to filter context such that the queries output is with respect to a given calling context. This improves precision when integrating into a context-sensitive analysis, such as a taint or typestate analysis.

# Examples

We have prepared a couple of examples on how to use Boomerang. They can be found [here](example/example/Example.java).

# Licencse
Boomerang is released under LGPL - see [LICENSE.txt](LICENSE.txt) for details.

# Authors
Boomerang has been developed by [Johannes Späth](mailto:joh.spaeth@gmail.com), Lisa Nguyen Quang Do, [Karim Ali](http://karimali.ca) and [Eric Bodden](bodden.de).
