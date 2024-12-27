# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
from typing import Any
from typing import Optional
np = import_numpy()

# Markers for events during the game indicated by the user
class TimelineMarker(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset: int = 0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = TimelineMarker()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsTimelineMarker(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # TimelineMarker
    def Init(self, buf: bytes, pos: int):
        self._tab = flatbuffers.table.Table(buf, pos)

    # TimelineMarker
    def Round(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

    # TimelineMarker
    def ColorHex(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.Get(flatbuffers.number_types.Int32Flags, o + self._tab.Pos)
        return 0

    # TimelineMarker
    def Label(self) -> Optional[str]:
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.String(o + self._tab.Pos)
        return None

def TimelineMarkerStart(builder: flatbuffers.Builder):
    builder.StartObject(3)

def Start(builder: flatbuffers.Builder):
    TimelineMarkerStart(builder)

def TimelineMarkerAddRound(builder: flatbuffers.Builder, round: int):
    builder.PrependInt32Slot(0, round, 0)

def AddRound(builder: flatbuffers.Builder, round: int):
    TimelineMarkerAddRound(builder, round)

def TimelineMarkerAddColorHex(builder: flatbuffers.Builder, colorHex: int):
    builder.PrependInt32Slot(1, colorHex, 0)

def AddColorHex(builder: flatbuffers.Builder, colorHex: int):
    TimelineMarkerAddColorHex(builder, colorHex)

def TimelineMarkerAddLabel(builder: flatbuffers.Builder, label: int):
    builder.PrependUOffsetTRelativeSlot(2, flatbuffers.number_types.UOffsetTFlags.py_type(label), 0)

def AddLabel(builder: flatbuffers.Builder, label: int):
    TimelineMarkerAddLabel(builder, label)

def TimelineMarkerEnd(builder: flatbuffers.Builder) -> int:
    return builder.EndObject()

def End(builder: flatbuffers.Builder) -> int:
    return TimelineMarkerEnd(builder)
