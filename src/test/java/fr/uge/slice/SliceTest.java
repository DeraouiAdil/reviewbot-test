package fr.uge.slice;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import module java.base;

import static org.junit.jupiter.api.Assertions.*;

public final class SliceTest {

  @Nested
  public class Q1 {
    @Test
    public void sliceOfStrings() {
      var array = new String[] { "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 3);
      assertEquals("[bar, baz]", slice.toString());
    }

    @Test
    public void sliceOfIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 4, 9);
      assertEquals("[4, 5, 6, 7, 8]", slice.toString());
    }

    @Test
    public void sliceSingleton() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 2);
      assertEquals("[bar]", slice.toString());
    }

    @Test
    public void sliceWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertEquals("[foo, null, bar]", slice.toString());
    }

    @Test
    public void sliceEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      assertEquals("[]", slice.toString());
    }

    @Test
    public void sliceIsAView() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, 2);
      array[1] = "hello";
      assertEquals("[foo, hello]", slice.toString());
    }

    @Test
    public void preconditionOf() {
      var array = new String[] { "foo", "bar" };
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> Slice.of(null, 1, 2)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice.of(array, 3, 3)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice.of(array, -1, 1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice.of(array, 1, 0)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> Slice.of(array, 2, 0))
      );
    }

    @Test
    public void spliceImplIsAMemberClass() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertAll(
          () -> assertTrue(slice.getClass().accessFlags().contains(AccessFlag.FINAL)),
          () -> assertTrue(slice.getClass().accessFlags().contains(AccessFlag.STATIC)),
          () -> assertTrue(slice.getClass().isMemberClass())
      );
    }

    @Test
    public void sliceImplConstructorIsNotPublic() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertEquals(0, slice.getClass().getConstructors().length);
    }

    @Test
    public void sliceImplClassConstructorCallSuperAsLastInstruction() throws IOException {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      var sliceImpl = slice.getClass();

      var className = "/" + sliceImpl.getName().replace('.', '/') + ".class";
      byte[] data;
      try(var input = sliceImpl.getResourceAsStream(className)) {
        if (input == null) {
          throw new AssertionError("Could not read class file");
        }
        data = input.readAllBytes();
      }
      var classModel = ClassFile.of().parse(data);
      var constructors =
          classModel.methods().stream().filter(m -> m.methodName().equalsString("<init>")).toList();
      for (var constructor : constructors) {
        var code =
            constructor.code().orElseThrow(() -> new AssertionError("Constructor has no code"));
        var instructions =
            code.elementStream()
                .flatMap(e -> e instanceof Instruction instruction ? Stream.of(instruction) : null)
                .toList();
        var lastInstruction =
            instructions.get(instructions.size() - 2); // -2 because last is RETURN
        if (!(lastInstruction instanceof InvokeInstruction invokeInstruction)) {
          throw new AssertionError(
              "lastInstruction is neither super() nor this() " + lastInstruction);
        }
        assertAll(
            () -> assertEquals(Opcode.INVOKESPECIAL, invokeInstruction.opcode()),
            () -> assertEquals("<init>", invokeInstruction.name().stringValue())
        );
      }
    }
  }


  @Nested
  public class Q2 {
    @Test
    public void sliceOfStrings() {
      String[] array = new String[] { "foo", "bar", "baz", "whizz" };
      Slice<String> slice = Slice.of(array, 1, 3);
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("bar", slice.get(0)),
          () -> assertEquals("baz", slice.get(1))
      );
    }

    @Test
    public void sliceOfIntegers() {
      Integer[] array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      Slice<Integer> slice = Slice.of(array, 3, 7);
      assertAll(
          () -> assertEquals(4, slice.size()),
          () -> assertEquals(3, slice.get(0)),
          () -> assertEquals(4, slice.get(1)),
          () -> assertEquals(5, slice.get(2)),
          () -> assertEquals(6, slice.get(3))
      );
    }

    @Test
    public void sliceWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 1, array.length);
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertNull(slice.get(0)),
          () -> assertEquals("bar", slice.get(1))
      );
    }

    @Test
    public void sliceEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      assertEquals(0, slice.size());
    }

    @Test
    public void sliceEmpty2() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 2, 2);
      assertEquals(0, slice.size());
    }

    @Test
    public void sliceIsAView() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, 2);
      array[0] = "hello";
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("hello", slice.get(0)),
          () -> assertEquals("bar", slice.get(1))
      );
    }

    @Test
    public void preconditionGet() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(2))
      );
    }

    @Test
    public void preconditionGet2() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, array.length);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(1))
      );
    }
  }


  @Nested
  public class Q3 {
    @Test
    public void replaceAllStrings() {
      var array = new String[]{ "foo", " bar", "baz ", "whizz" };
      var slice = Slice.of(array, 1, 3);
      slice.replaceAll(String::trim);
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("bar", slice.get(0)),
          () -> assertEquals("baz", slice.get(1))
      );
    }

    @Test
    public void replaceAllIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 3, 7);
      slice.replaceAll(Math::negateExact);
      assertAll(
          () -> assertEquals(4, slice.size()),
          () -> assertEquals(-3, slice.get(0)),
          () -> assertEquals(-4, slice.get(1)),
          () -> assertEquals(-5, slice.get(2)),
          () -> assertEquals(-6, slice.get(3))
      );
    }

    @Test
    public void replaceAllIntegersHasSideEffect() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 3, 7);
      slice.replaceAll(Math::negateExact);
      assertAll(
          () -> assertEquals(0, array[0]),
          () -> assertEquals(1, array[1]),
          () -> assertEquals(2, array[2]),
          () -> assertEquals(-3, array[3]),
          () -> assertEquals(-4, array[4]),
          () -> assertEquals(-5, array[5]),
          () -> assertEquals(-6, array[6]),
          () -> assertEquals(7, array[7]),
          () -> assertEquals(8, array[8]),
          () -> assertEquals(9, array[9])
      );
    }

    @Test
    public void replaceAllWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 0, array.length);
      slice.replaceAll(String::valueOf);
      assertAll(
          () -> assertEquals(3, slice.size()),
          () -> assertEquals("foo", slice.get(0)),
          () -> assertEquals("null", slice.get(1)),
          () -> assertEquals("bar", slice.get(2))
      );
    }

    @Test
    public void replaceAllEmpty() {
      var array = new String[] { null, null };
      var slice = Slice.of(array, 1, 1);
      slice.replaceAll(Objects::requireNonNull);
    }

    @Test
    public void preconditionReplaceAll() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertThrows(NullPointerException.class, () -> slice.replaceAll(null));
    }

    @Test
    public void preconditionReplaceAllEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      assertThrows(NullPointerException.class, () -> slice.replaceAll(null));
    }

    @Test
    public void qualityOfImplementation() {
      var replaceAll = Arrays.stream(Slice.class.getMethods())
          .filter(m -> m.getName().equals("replaceAll"))
          .findFirst()
          .orElseThrow();
      assertEquals(1, replaceAll.getParameterCount());
      assertNotEquals(Function.class, replaceAll.getParameterTypes()[0]);
    }
  }


  @Nested
  public class Q4 {
    @Test
    public void reversedOfStrings() {
      var array = new String[]{ "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 4);
      var reversed = slice.reversed();
      assertAll(
          () -> assertEquals(3, reversed.size()),
          () -> assertEquals("whizz", reversed.get(0)),
          () -> assertEquals("baz", reversed.get(1)),
          () -> assertEquals("bar", reversed.get(2))
      );
    }

    @Test
    public void reversedOfIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 3, 7);
      var reversed = slice.reversed();
      assertAll(
          () -> assertEquals(4, reversed.size()),
          () -> assertEquals(6, reversed.get(0)),
          () -> assertEquals(5, reversed.get(1)),
          () -> assertEquals(4, reversed.get(2)),
          () -> assertEquals(3, reversed.get(3))
      );
    }

    @Test
    public void reversedWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 0, array.length);
      var reversed = slice.reversed();
      assertAll(
          () -> assertEquals(3, reversed.size()),
          () -> assertEquals("bar", reversed.get(0)),
          () -> assertNull(reversed.get(1)),
          () -> assertEquals("foo", reversed.get(2))
      );
    }

    @Test
    public void reversedEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      var reversed = slice.reversed();
      assertEquals(0, reversed.size());
    }

    @Test
    public void reversedReplaceAllStrings() {
      var array = new String[]{ "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 4);
      var reversed = slice.reversed();
      reversed.replaceAll(x -> "*" + x + "*");
      assertAll(
          () -> assertEquals(3, reversed.size()),
          () -> assertEquals("*whizz*", reversed.get(0)),
          () -> assertEquals("*baz*", reversed.get(1)),
          () -> assertEquals("*bar*", reversed.get(2))
      );
    }

    @Test
    public void reverseReplaceAllIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 3, 7);
      var reversed = slice.reversed();
      reversed.replaceAll(x -> 2 * x);
      assertAll(
          () -> assertEquals(4, reversed.size()),
          () -> assertEquals(12, reversed.get(0)),
          () -> assertEquals(10, reversed.get(1)),
          () -> assertEquals(8, reversed.get(2)),
          () -> assertEquals(6, reversed.get(3))
      );
    }

    @Test
    public void preconditionReverseReplaceAll() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertThrows(NullPointerException.class, () -> slice.reversed().replaceAll(null));
    }

    @Test
    public void preconditionReverseReplaceAllEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      assertThrows(NullPointerException.class, () -> slice.reversed().replaceAll(null));
    }

    @Test
    public void qualityOfImplementation() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      var reversed = slice.reversed();
      assertTrue(reversed.getClass().isAnonymousClass());
    }
  }


  @Nested
  public class Q5 {
    @Test
    public void reversedReversed() {
      var array = new Integer[] { 0, 1, 2, 3, 4 };
      var slice = Slice.of(array, 0, array.length);
      assertSame(slice, slice.reversed().reversed());
    }
  }


  @Nested
  public class Q6 {
    @Test
    public void subSliceOfStrings() {
      var array = new String[]{ "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 4);
      var subSlice = slice.subSlice(1, 2);
      assertAll(
          () -> assertEquals(1, subSlice.size()),
          () -> assertEquals("baz", subSlice.get(0))
      );
    }

    @Test
    public void subSliceOfIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 3, 7);
      var subSlice = slice.subSlice(1, 3);
      assertAll(
          () -> assertEquals(2, subSlice.size()),
          () -> assertEquals(4, subSlice.get(0)),
          () -> assertEquals(5, subSlice.get(1))
      );
    }

    @Test
    public void subSliceWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 0, array.length);
      var subSlice = slice.subSlice(1, 3);
      assertAll(
          () -> assertEquals(2, subSlice.size()),
          () -> assertNull(subSlice.get(0)),
          () -> assertEquals("bar", subSlice.get(1))
      );
    }

    @Test
    public void subSliceEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, 2);
      var subSlice = slice.subSlice(1, 1);
      assertEquals(0, subSlice.size());
    }

    @Test
    public void subSliceIsAView() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, 2);
      var subSlice = slice.subSlice(1, 2);
      array[1] = "hello";
      assertEquals("hello", subSlice.get(0));
    }

    @Test
    public void preconditionSubSlice() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, 2);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(3, 3)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(-1, 1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(1, 0)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(2, 0))
      );
    }

    @Test
    public void preconditionSubSlice2() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 2);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(2, 2)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(-1, 0)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.subSlice(1, 0))
      );
    }

    @Test
    public void preconditionSubSliceGet() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      var subSlice = slice.subSlice(1, array.length);
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subSlice.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> subSlice.get(1))
      );
    }

    @Test
    public void qualityOfImplementation() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      var subSlice = slice.subSlice(0, array.length);
      assertSame(subSlice.getClass(), slice.getClass());
    }

    @Test
    public void subSliceReplaceAllStrings() {
      var array = new String[]{ "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 4);
      var subslice = slice.subSlice(1, 3);
      subslice.replaceAll(x -> "*" + x + "*");
      assertAll(
          () -> assertEquals(2, subslice.size()),
          () -> assertEquals("*baz*", subslice.get(0)),
          () -> assertEquals("*whizz*", subslice.get(1))
      );
    }

    @Test
    public void subSliceReplaceAllIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 2, 7);
      var subslice = slice.subSlice(1, 5);
      slice.replaceAll(x -> 2 * x);
      assertAll(
          () -> assertEquals(4, subslice.size()),
          () -> assertEquals(6, subslice.get(0)),
          () -> assertEquals(8, subslice.get(1)),
          () -> assertEquals(10, subslice.get(2)),
          () -> assertEquals(12, subslice.get(3))
      );
    }

    @Test
    public void preconditionSubSliceReplaceAll() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> slice.subSlice(0, array.length).replaceAll(null))
      );
    }

    @Test
    public void preconditionSubSliceReplaceAllEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      assertAll(
          () -> assertThrows(NullPointerException.class, () -> slice.subSlice(0, 0).replaceAll(null))
      );
    }

    @Test
    public void reversedSliceOfStrings() {
      var array = new String[] { "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 3);
      var reversed = slice.reversed();
      assertEquals("[baz, bar]", reversed.toString());
    }

    @Test
    public void reversedSliceOfIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 4, 9);
      var reversed = slice.reversed();
      assertEquals("[8, 7, 6, 5, 4]", reversed.toString());
    }

    @Test
    public void reversedSliceSingleton() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 2);
      var reversed = slice.reversed();
      assertEquals("[bar]", reversed.toString());
    }

    @Test
    public void reversedSliceWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 0, array.length);
      var reversed = slice.reversed();
      assertEquals("[bar, null, foo]", reversed.toString());
    }

    @Test
    public void reversedSliceEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      var reversed = slice.reversed();
      assertEquals("[]", reversed.toString());
    }

    @Test
    public void reversedSliceIsAView() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, 2);
      var reversed = slice.reversed();
      array[1] = "hello";
      assertEquals("[hello, foo]", reversed.toString());
    }
  }


  @Nested
  public class Q7 {
    @Test
    public void reversedSubSliceOfStrings() {
      var array = new String[]{ "foo", "bar", "baz", "whizz" };
      var slice = Slice.of(array, 1, 4);
      var reversed = slice.reversed();
      var subSlice = reversed.subSlice(1, 3);
      assertAll(
          () -> assertEquals(2, subSlice.size()),
          () -> assertEquals("baz", subSlice.get(0)),
          () -> assertEquals("bar", subSlice.get(1))
      );
    }

    @Test
    public void reversedSubSliceOfIntegers() {
      var array = new Integer[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
      var slice = Slice.of(array, 3, 8);
      var reversed = slice.reversed();
      var subSlice = reversed.subSlice(0, 4);
      assertAll(
          () -> assertEquals(4, subSlice.size()),
          () -> assertEquals(7, subSlice.get(0)),
          () -> assertEquals(6, subSlice.get(1)),
          () -> assertEquals(5, subSlice.get(2)),
          () -> assertEquals(4, subSlice.get(3))
      );
    }

    @Test
    public void reversedSubSliceWithNull() {
      var array = new String[] { "foo", null, "bar" };
      var slice = Slice.of(array, 0, array.length);
      var reversed = slice.reversed();
      var subSlice = reversed.subSlice(1, 3);
      assertAll(
          () -> assertEquals(2, subSlice.size()),
          () -> assertNull(subSlice.get(0)),
          () -> assertEquals("foo", subSlice.get(1))
      );
    }

    @Test
    public void reversedSubSliceEmpty() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 1, 1);
      var subSlice = slice.reversed().subSlice(0, 0);
      assertEquals(0, subSlice.size());
    }

    @Test
    public void reversedSubSliceQualityOfImplementation() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      assertSame(slice.reversed().getClass(), slice.reversed().subSlice(1, 2).getClass());
    }
  }


  @Nested
  public class Q8 {
    @Test
    public void sliceOfStrings() {
      Slice<String> slice = Slice.of("foo", "bar", "baz", "whizz");
      assertAll(
          () -> assertEquals(4, slice.size()),
          () -> assertEquals("foo", slice.get(0)),
          () -> assertEquals("bar", slice.get(1)),
          () -> assertEquals("baz", slice.get(2)),
          () -> assertEquals("whizz", slice.get(3))
      );
    }

    @Test
    public void sliceOfIntegers() {
      Slice<Integer> slice = Slice.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
      assertAll(
          () -> assertEquals(10, slice.size()),
          () -> assertEquals(0, slice.get(0)),
          () -> assertEquals(1, slice.get(1)),
          () -> assertEquals(2, slice.get(2)),
          () -> assertEquals(3, slice.get(3)),
          () -> assertEquals(4, slice.get(4)),
          () -> assertEquals(5, slice.get(5)),
          () -> assertEquals(6, slice.get(6)),
          () -> assertEquals(7, slice.get(7)),
          () -> assertEquals(8, slice.get(8)),
          () -> assertEquals(9, slice.get(9))
      );
    }

    @Test
    public void sliceOfWithNull() {
      var slice = Slice.of("foo", null, "bar");
      assertAll(
          () -> assertEquals(3, slice.size()),
          () -> assertEquals("foo", slice.get(0)),
          () -> assertNull(slice.get(1)),
          () -> assertEquals("bar", slice.get(2))
      );
    }

    @Test
    public void sliceOfEmpty() {
      var slice = Slice.of();
      assertEquals(0, slice.size());
    }

    @Test
    public void sliceOfAParametrizedType() {
      var slice = Slice.of(Set.of(1, 2));
      assertAll(
          () -> assertEquals(1, slice.size()),
          () -> assertEquals(Set.of(1, 2), slice.get(0))
      );
    }

    @Test
    public void sliceOfIsAView() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array);
      array[0] = "hello";
      assertAll(
          () -> assertEquals(2, slice.size()),
          () -> assertEquals("hello", slice.get(0)),
          () -> assertEquals("bar", slice.get(1))
      );
    }

    @Test
    public void sliceOfPreconditionOf() {
      assertThrows(NullPointerException.class, () -> Slice.of((String[]) null));
    }

    @Test
    public void sliceOfPreconditionEmptyGet() {
      var slice = Slice.of();
      assertAll(
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(-1)),
          () -> assertThrows(IndexOutOfBoundsException.class, () -> slice.get(0))
      );
    }

    @Test
    public void sliceOfQualityOfImplementation() {
      var array = new String[] { "foo", "bar" };
      var slice = Slice.of(array, 0, array.length);
      var slice2 = Slice.of(array);
      assertSame(slice.getClass(), slice2.getClass());
    }
  }
}