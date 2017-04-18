package test.cases.array;

import org.junit.Test;

import test.core.selfrunning.AbstractBoomerangTest;
import test.core.selfrunning.AllocatedObject;

public class ArrayTest extends AbstractBoomerangTest{

  public static class A implements AllocatedObject{
  }

  @Test
  public  void simpleAssignment() {
    Object[] array = new Object[3];
    A alias = new A();
    array[1] = alias;
    Object query = array[2];
    queryFor(query);
  }

  @Test
  public  void indexInsensitive() {
    Object[] array = new Object[3];
    A alias1 = new A();
    A alias2 = new A();
    array[1] = alias1;
    array[2] = alias2;
    Object query = array[2];
    queryFor(query);
  }
  @Test
  public void doubleArray() {
    Object[][] array = new Object[3][3];
    array[1][2] = new A();
    array[2][3] = new A();
    Object query = array[3][3];
    queryFor(query);
  }

  @Test
  public void arrayCopyTest() {
    Object[] copiedArray = new Object[3];
    Object[] originalArray = new Object[3];
    A alias = new A();
    originalArray[1] = alias;
    System.arraycopy(originalArray, 0, copiedArray, 0, 1);
    Object query = copiedArray[3];
    queryFor(query);
  }
}
