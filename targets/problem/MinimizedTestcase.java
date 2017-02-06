package problem;


public class MinimizedTestcase {
  public static void main(String... args) {
    // this method should be used only when we run out of a state.

    State s = new State(null);
    State t = s;
    for (int i = 0; i < 8; i++)
      s = new State(t);
  }

}
