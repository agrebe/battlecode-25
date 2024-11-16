# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# Indicate that this robot was spawned on this turn
class SpawnAction(object):
    __slots__ = ['_tab']

    @classmethod
    def SizeOf(cls):
        return 6

    # SpawnAction
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # SpawnAction
    def X(self): return self._tab.Get(flatbuffers.number_types.Uint16Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(0))
    # SpawnAction
    def Y(self): return self._tab.Get(flatbuffers.number_types.Uint16Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(2))
    # SpawnAction
    def Team(self): return self._tab.Get(flatbuffers.number_types.Int8Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(4))
    # SpawnAction
    def RobotType(self): return self._tab.Get(flatbuffers.number_types.Int8Flags, self._tab.Pos + flatbuffers.number_types.UOffsetTFlags.py_type(5))

def CreateSpawnAction(builder, x, y, team, robotType):
    builder.Prep(2, 6)
    builder.PrependInt8(robotType)
    builder.PrependInt8(team)
    builder.PrependUint16(y)
    builder.PrependUint16(x)
    return builder.Offset()
