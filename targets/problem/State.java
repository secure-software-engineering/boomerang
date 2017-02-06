package problem;

public class State {
  public State prev;
  private State next;


  public State(State prev) {
    this.prev = prev;
    if (prev != null)
      prev.next = this;
  }
}
