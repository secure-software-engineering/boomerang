package problem;

public class Test {
  private State current;
  private State root;

  public static void main(String... args) {
    Test test = new Test();
    State state = test.new State(null);
  }
  public Test() {
    this.root = this.current = new State(null);
    allocateMoreStates();
  }
  /**
   * Allocates a few more {@link State}s.
   *
   * Allocating multiple {@link State}s at once allows those objects to be allocated near each
   * other, which reduces the working set of CPU. It improves the chance the relevant data is in the
   * cache.
   */
  private void allocateMoreStates() {
    // this method should be used only when we run out of a state.
    assert current.next == null;

    State s = current;
    State t = s;
    for (int i = 0; i < 8; i++)
      s = new State(t);
  }


  public final class State {
    public State prev;
    private State next;


    State(State prev) {
      this.prev = prev;
      if (prev != null)
        prev.next = this;
    }

  }
}
