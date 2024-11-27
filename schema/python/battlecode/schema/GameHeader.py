# automatically generated by the FlatBuffers compiler, do not modify

# namespace: schema

import flatbuffers
from flatbuffers.compat import import_numpy
np = import_numpy()

# The first event sent in the game. Contains all metadata about the game.
class GameHeader(object):
    __slots__ = ['_tab']

    @classmethod
    def GetRootAs(cls, buf, offset=0):
        n = flatbuffers.encode.Get(flatbuffers.packer.uoffset, buf, offset)
        x = GameHeader()
        x.Init(buf, n + offset)
        return x

    @classmethod
    def GetRootAsGameHeader(cls, buf, offset=0):
        """This method is deprecated. Please switch to GetRootAs."""
        return cls.GetRootAs(buf, offset)
    # GameHeader
    def Init(self, buf, pos):
        self._tab = flatbuffers.table.Table(buf, pos)

    # GameHeader
    def SpecVersion(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(4))
        if o != 0:
            return self._tab.String(o + self._tab.Pos)
        return None

    # GameHeader
    def Teams(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            x = self._tab.Vector(o)
            x += flatbuffers.number_types.UOffsetTFlags.py_type(j) * 4
            x = self._tab.Indirect(x)
            from battlecode.schema.TeamData import TeamData
            obj = TeamData()
            obj.Init(self._tab.Bytes, x)
            return obj
        return None

    # GameHeader
    def TeamsLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # GameHeader
    def TeamsIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(6))
        return o == 0

    # GameHeader
    def RobotTypeMetadata(self, j):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            x = self._tab.Vector(o)
            x += flatbuffers.number_types.UOffsetTFlags.py_type(j) * 4
            x = self._tab.Indirect(x)
            from battlecode.schema.RobotTypeMetadata import RobotTypeMetadata
            obj = RobotTypeMetadata()
            obj.Init(self._tab.Bytes, x)
            return obj
        return None

    # GameHeader
    def RobotTypeMetadataLength(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        if o != 0:
            return self._tab.VectorLen(o)
        return 0

    # GameHeader
    def RobotTypeMetadataIsNone(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(8))
        return o == 0

    # GameHeader
    def Constants(self):
        o = flatbuffers.number_types.UOffsetTFlags.py_type(self._tab.Offset(10))
        if o != 0:
            x = self._tab.Indirect(o + self._tab.Pos)
            from battlecode.schema.GameplayConstants import GameplayConstants
            obj = GameplayConstants()
            obj.Init(self._tab.Bytes, x)
            return obj
        return None

def GameHeaderStart(builder):
    builder.StartObject(4)

def Start(builder):
    GameHeaderStart(builder)

def GameHeaderAddSpecVersion(builder, specVersion):
    builder.PrependUOffsetTRelativeSlot(0, flatbuffers.number_types.UOffsetTFlags.py_type(specVersion), 0)

def AddSpecVersion(builder, specVersion):
    GameHeaderAddSpecVersion(builder, specVersion)

def GameHeaderAddTeams(builder, teams):
    builder.PrependUOffsetTRelativeSlot(1, flatbuffers.number_types.UOffsetTFlags.py_type(teams), 0)

def AddTeams(builder, teams):
    GameHeaderAddTeams(builder, teams)

def GameHeaderStartTeamsVector(builder, numElems):
    return builder.StartVector(4, numElems, 4)

def StartTeamsVector(builder, numElems):
    return GameHeaderStartTeamsVector(builder, numElems)

def GameHeaderAddRobotTypeMetadata(builder, robotTypeMetadata):
    builder.PrependUOffsetTRelativeSlot(2, flatbuffers.number_types.UOffsetTFlags.py_type(robotTypeMetadata), 0)

def AddRobotTypeMetadata(builder, robotTypeMetadata):
    GameHeaderAddRobotTypeMetadata(builder, robotTypeMetadata)

def GameHeaderStartRobotTypeMetadataVector(builder, numElems):
    return builder.StartVector(4, numElems, 4)

def StartRobotTypeMetadataVector(builder, numElems):
    return GameHeaderStartRobotTypeMetadataVector(builder, numElems)

def GameHeaderAddConstants(builder, constants):
    builder.PrependUOffsetTRelativeSlot(3, flatbuffers.number_types.UOffsetTFlags.py_type(constants), 0)

def AddConstants(builder, constants):
    GameHeaderAddConstants(builder, constants)

def GameHeaderEnd(builder):
    return builder.EndObject()

def End(builder):
    return GameHeaderEnd(builder)
