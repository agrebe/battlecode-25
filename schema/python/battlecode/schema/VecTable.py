# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

class VecTable(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = VecTable()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsVecTable(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # VecTable
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # VecTable
    def Xs(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int32Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 4))
        return 0

    # VecTable
    def XsAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int32Flags, o)
        return 0

    # VecTable
    def XsLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # VecTable
    def XsIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        return o == 0

    # VecTable
    def Ys(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            a = self._tab.Vector(o)
            return self._tab.Get(flatbuffers.number_types.Int32Flags, a + flatbuffers.number_types.UOffsetTFlags.py_type(j * 4))
        return 0

    # VecTable
    def YsAsNumpy(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.GetVectorAsNumpy(flatbuffers.number_types.Int32Flags, o)
        return 0

    # VecTable
    def YsLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # VecTable
    def YsIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        return o == 0

def VecTableStart(builder):
    builder.StartObject(2)

def Start(builder):
    VecTableStart(builder)

def VecTableAddXs(builder, xs):
    builder.PrependUOffsetTRelativeSlot(0, flatbuffers.number_types.UOffsetTFlags.py_type(xs), 0)

def AddXs(builder, xs):
    VecTableAddXs(builder, xs)

def VecTableStartXsVector(builder, numElems):
    return builder.StartVector(4, numElems, 4)

def StartXsVector(builder, numElems):
    return VecTableStartXsVector(builder, numElems)

def VecTableAddYs(builder, ys):
    builder.PrependUOffsetTRelativeSlot(1, flatbuffers.number_types.UOffsetTFlags.py_type(ys), 0)

def AddYs(builder, ys):
    VecTableAddYs(builder, ys)

def VecTableStartYsVector(builder, numElems):
    return builder.StartVector(4, numElems, 4)

def StartYsVector(builder, numElems):
    return VecTableStartYsVector(builder, numElems)

def VecTableEnd(builder):
    return builder.EndObject()

def End(builder):
    return VecTableEnd(builder)
