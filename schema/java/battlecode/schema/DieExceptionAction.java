// automatically generated by the FlatBuffers compiler, do not modify

package battlecode.schema;

import com.google.flatbuffers.BaseVector;
import com.google.flatbuffers.BooleanVector;
import com.google.flatbuffers.ByteVector;
import com.google.flatbuffers.Constants;
import com.google.flatbuffers.DoubleVector;
import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.FloatVector;
import com.google.flatbuffers.IntVector;
import com.google.flatbuffers.LongVector;
import com.google.flatbuffers.ShortVector;
import com.google.flatbuffers.StringVector;
import com.google.flatbuffers.Struct;
import com.google.flatbuffers.Table;
import com.google.flatbuffers.UnionVector;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Indicates that the robot died due to an uncaught exception
 */
@SuppressWarnings("unused")
public final class DieExceptionAction extends Struct {
  public void __init(int _i, ByteBuffer _bb) { __reset(_i, _bb); }
  public DieExceptionAction __assign(int _i, ByteBuffer _bb) { __init(_i, _bb); return this; }

  public byte value() { return bb.get(bb_pos + 0); }

  public static int createDieExceptionAction(FlatBufferBuilder builder, byte value) {
    builder.prep(1, 1);
    builder.putByte(value);
    return builder.offset();
  }

  public static final class Vector extends BaseVector {
    public Vector __assign(int _vector, int _element_size, ByteBuffer _bb) { __reset(_vector, _element_size, _bb); return this; }

    public DieExceptionAction get(int j) { return get(new DieExceptionAction(), j); }
    public DieExceptionAction get(DieExceptionAction obj, int j) {  return obj.__assign(__element(j), bb); }
  }
}

