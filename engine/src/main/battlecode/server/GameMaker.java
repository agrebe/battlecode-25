package battlecode.server;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.ResourceType;
import battlecode.common.RobotType;
import battlecode.common.SkillType;
import battlecode.common.TrapType;
import battlecode.common.Team;
import battlecode.instrumenter.profiler.Profiler;
import battlecode.instrumenter.profiler.ProfilerCollection;
import battlecode.instrumenter.profiler.ProfilerEventType;
import battlecode.schema.*;
import battlecode.util.FlatHelpers;
import battlecode.util.TeamMapping;
import battlecode.world.*;
import com.google.flatbuffers.FlatBufferBuilder;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.function.ToIntFunction;
import java.util.zip.GZIPOutputStream;

import static battlecode.util.FlatHelpers.*;

/**
 * Writes a game to a flatbuffer, hooray.
 */
public strictfp class GameMaker {

    /**
     * The protocol expects a series of valid state transitions;
     * we ensure that's true.
     */
    private enum State {
        /**
         * Waiting to write game header.
         */
        GAME_HEADER,
        /**
         * In a game, but not a match.
         */
        IN_GAME,
        /**
         * In a match.
         */
        IN_MATCH,
        /**
         * Complete.
         */
        DONE
    }
    private State state;

    // this un-separation-of-concerns makes me uncomfortable

    /**
     * We write the whole match to this builder, then write it to a file.
     */
    private final FlatBufferBuilder fileBuilder;

    /**
     * Null until the end of the match.
     */
    private byte[] finishedGame;

    /**
     * We have a separate byte[] for each packet sent to the client.
     * This is necessary because flatbuffers shares metadata between structures, so we
     * can't just cut out chunks of the larger buffer :/
     */
    private FlatBufferBuilder packetBuilder;

    /**
     * The server we're sending packets on.
     * May be null.
     */
    private final NetServer packetSink;

    /**
     * Information about the active game.
     */
    private final GameInfo gameInfo;

    /**
     * Only relevant to the file builder:
     * We add a table called a GameWrapper to the front of the saved files
     * that lets you quickly navigate to events by index, and tells you the
     * indices of headers and footers.
     */
    private TIntArrayList events;
    private TIntArrayList matchHeaders;
    private TIntArrayList matchFooters;

    /**
     * The MatchMaker associated with this GameMaker.
     */
    private final MatchMaker matchMaker;

    /**
     * Whether to serialize indicator dots and lines into the flatbuffer.
     */
    private final boolean showIndicators;

    /**
     * @param gameInfo the mapping of teams to bytes
     * @param packetSink the NetServer to send packets to
     * @param showIndicators whether to write indicator dots and lines to replay
     */
    public GameMaker(final GameInfo gameInfo, final NetServer packetSink, final boolean showIndicators) {
        this.state = State.GAME_HEADER;

        this.gameInfo = gameInfo;

        this.packetSink = packetSink;
        if (packetSink != null) {
            this.packetBuilder = new FlatBufferBuilder();
        }

        this.fileBuilder = new FlatBufferBuilder();

        this.events = new TIntArrayList();
        this.matchHeaders = new TIntArrayList();
        this.matchFooters = new TIntArrayList();

        this.matchMaker = new MatchMaker();

        this.showIndicators = showIndicators;
    }

    /**
     * Assert we're in a particular state.
     *
     * @param state
     */
    private void assertState(State state) {
        if (this.state != state) {
            throw new RuntimeException("Incorrect GameMaker state: should be "+
                    state+", but is: "+this.state);
        }
    }

    /**
     * Make a state transition.
     */
    private void changeState(State start, State end) {
        assertState(start);
        this.state = end;
    }


    /**
     * Convert entire game to a byte array.
     *
     * @return game as a packed flatbuffer byte array.
     */
    public byte[] toBytes() {
        if (finishedGame == null) {
            assertState(State.DONE);

            int events = GameWrapper.createEventsVector(fileBuilder, this.events.toArray());
            int matchHeaders = GameWrapper.createMatchHeadersVector(fileBuilder, this.matchHeaders.toArray());
            int matchFooters = GameWrapper.createMatchFootersVector(fileBuilder, this.matchFooters.toArray());

            GameWrapper.startGameWrapper(fileBuilder);
            GameWrapper.addEvents(fileBuilder, events);
            GameWrapper.addMatchHeaders(fileBuilder, matchHeaders);
            GameWrapper.addMatchFooters(fileBuilder, matchFooters);

            fileBuilder.finish(GameWrapper.endGameWrapper(fileBuilder));

            byte[] rawBytes = fileBuilder.sizedByteArray();

            try {
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                GZIPOutputStream zipper = new GZIPOutputStream(result);
                IOUtils.copy(new ByteArrayInputStream(rawBytes), zipper);
                zipper.close();
                zipper.flush();
                result.flush();
                finishedGame = result.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("Gzipping failed?", e);
            }
        }
        return finishedGame;
    }

    /**
     * Write a match out to a file.
     *
     * @param saveFile the file to save to
     */
    public void writeGame(File saveFile) {
        if (saveFile == null) {
            throw new RuntimeException("Null file provided to writeGame");
        }

        try {
            FileUtils.writeByteArrayToFile(saveFile, toBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Run the same logic for both builders.
     *
     * @param perBuilder called with each builder; return event id. Should not mutate state.
     */
    private void createEvent(ToIntFunction<FlatBufferBuilder> perBuilder) {
        // make file event and add its offset to the list
        int eventAP = perBuilder.applyAsInt(fileBuilder);
        events.add(eventAP);

        if (packetSink != null) {
            // make packet event and package it up
            int eventBP = perBuilder.applyAsInt(packetBuilder);
            packetBuilder.finish(eventBP);
            packetSink.addEvent(packetBuilder.sizedByteArray());

            // reset packet builder
            packetBuilder = new FlatBufferBuilder(packetBuilder.dataBuffer());
        }
    }

    /**
     * Get the MatchMaker associated with this GameMaker.
     */
    public MatchMaker getMatchMaker() {
        return this.matchMaker;
    }

    public void makeGameHeader() {

        changeState(State.GAME_HEADER, State.IN_GAME);

        createEvent((builder) -> {
            int specVersionOffset = builder.createString(GameConstants.SPEC_VERSION);

            int name = builder.createString(gameInfo.getTeamAName());
            int packageName = builder.createString(gameInfo.getTeamAPackage());
            TeamData.startTeamData(builder);
            TeamData.addName(builder, name);
            TeamData.addPackageName(builder, packageName);
            TeamData.addTeamId(builder, TeamMapping.id(Team.A));

            int teamAOffset = TeamData.endTeamData(builder);

            name = builder.createString(gameInfo.getTeamBName());
            packageName = builder.createString(gameInfo.getTeamBPackage());
            TeamData.startTeamData(builder);
            TeamData.addName(builder, name);
            TeamData.addPackageName(builder, packageName);
            TeamData.addTeamId(builder, TeamMapping.id(Team.B));
            int teamBOffset = TeamData.endTeamData(builder);
            int[] teamsVec = {teamAOffset, teamBOffset};

            int teamsOffset = GameHeader.createTeamsVector(builder, teamsVec);
            int specializationMetadataOffset = makeSpecializationMetadata(builder);
            int buildActionMetadataOffset = makeBuildActionMetadata(builder);

            Constants.startConstants(builder);
            Constants.addSetupPhaseLength(builder, GameConstants.SETUP_ROUNDS);
            Constants.addFlagMinDistance(builder, GameConstants.MIN_FLAG_SPACING_SQUARED);
            Constants.addGlobalUpgradeRoundDelay(builder, GameConstants.GLOBAL_UPGRADE_ROUNDS);
            Constants.addPassiveResourceRate(builder, GameConstants.PASSIVE_BREAD_INCREASE);
            Constants.addRobotBaseHealth(builder, GameConstants.DEFAULT_HEALTH);
            Constants.addJailedRounds(builder, GameConstants.JAILED_ROUNDS);
            Constants.addVisionRadius(builder, GameConstants.VISION_RADIUS_SQUARED);
            Constants.addActionRadius(builder, GameConstants.ACTION_RADIUS_SQUARED);
            int constantsOffset = Constants.endConstants(builder);

            GameHeader.startGameHeader(builder);
            GameHeader.addSpecVersion(builder, specVersionOffset);
            GameHeader.addTeams(builder, teamsOffset);
            GameHeader.addSpecializationMetadata(builder, specializationMetadataOffset);
            GameHeader.addBuildActionMetadata(builder, buildActionMetadataOffset);
            //TODO global upgrade metadata
            GameHeader.addConstants(builder, constantsOffset);
            int gameHeaderOffset = GameHeader.endGameHeader(builder);

            return EventWrapper.createEventWrapper(builder, Event.GameHeader, gameHeaderOffset);
        });
    }

    public int makeSpecializationMetadata(FlatBufferBuilder builder) {
        TIntArrayList specializationMetadataOffsets = new TIntArrayList();

        for(SkillType type : SkillType.values()) {
            for(int l = 0; l <= 6; l++) {
                SpecializationMetadata.startSpecializationMetadata(builder);
                SpecializationMetadata.addType(builder, skillTypeToSpecializationType(type));
                SpecializationMetadata.addLevel(builder, l);
                //TODO not sure what "action cost" is in schema. No cost for building or healing, build cost depends on trap type but not level.
                SpecializationMetadata.addActionJailedPenalty(builder, type.getPenalty(l));
                SpecializationMetadata.addCooldownReduction(builder, type.getCooldown(l));
                int effect = type.getSkillEffect(l);
                if(type == SkillType.ATTACK) {
                    SpecializationMetadata.addDamageIncrease(builder, effect);
                    SpecializationMetadata.addHealIncrease(builder, 0);
                }
                else if(type == SkillType.BUILD) {
                    SpecializationMetadata.addDamageIncrease(builder, 0);
                    SpecializationMetadata.addHealIncrease(builder, 0);
                }
                else if(type == SkillType.HEAL) {
                    SpecializationMetadata.addDamageIncrease(builder, 0);
                    SpecializationMetadata.addHealIncrease(builder, effect);
                }
                specializationMetadataOffsets.add(SpecializationMetadata.endSpecializationMetadata(builder));
            }
        }
        return GameHeader.createSpecializationMetadataVector(builder, specializationMetadataOffsets.toArray());
    }

    public int makeBuildActionMetadata(FlatBufferBuilder builder) {
        TIntArrayList buildActionMetadataOffsets = new TIntArrayList();
        for(TrapType type : TrapType.values()) {
            BuildActionMetadata.startBuildActionMetadata(builder);
            BuildActionMetadata.addType(builder, trapTypeToBuildActionType(type));
            BuildActionMetadata.addCost(builder, type.buildCost);
            BuildActionMetadata.addBuildCooldown(builder, type.actionCooldownIncrease);
            buildActionMetadataOffsets.add(BuildActionMetadata.endBuildActionMetadata(builder));
        }
        BuildActionMetadata.startBuildActionMetadata(builder);
        BuildActionMetadata.addType(builder, BuildActionType.DIG);
        BuildActionMetadata.addCost(builder, GameConstants.DIG_COST);
        BuildActionMetadata.addBuildCooldown(builder, GameConstants.DIG_COOLDOWN);
        buildActionMetadataOffsets.add(BuildActionMetadata.endBuildActionMetadata(builder));
        BuildActionMetadata.startBuildActionMetadata(builder);
        BuildActionMetadata.addType(builder, BuildActionType.FILL);
        BuildActionMetadata.addCost(builder, GameConstants.FILL_COST);
        BuildActionMetadata.addBuildCooldown(builder, GameConstants.FILL_COOLDOWN);
        buildActionMetadataOffsets.add(BuildActionMetadata.endBuildActionMetadata(builder));
        return GameHeader.createBuildActionMetadataVector(builder, buildActionMetadataOffsets.toArray());
    }

    private byte robotTypeToBodyType(RobotType type) {
        if (type == RobotType.HEADQUARTERS) return BodyType.HEADQUARTERS;
        if (type == RobotType.AMPLIFIER) return BodyType.AMPLIFIER;
        if (type == RobotType.BOOSTER) return BodyType.BOOSTER;
        if (type == RobotType.CARRIER) return BodyType.CARRIER;
        if (type == RobotType.DESTABILIZER) return BodyType.DESTABILIZER;
        if (type == RobotType.LAUNCHER) return BodyType.LAUNCHER;
        return Byte.MIN_VALUE;
    }

    private byte skillTypeToSpecializationType(SkillType type) {
        if (type == SkillType.ATTACK) return SpecializationType.ATTACK;
        if (type == SkillType.BUILD) return SpecializationType.BUILD;
        if (type == SkillType.HEAL) return SpecializationType.HEAL;
        return Byte.MIN_VALUE;
    }

    private byte trapTypeToBuildActionType(TrapType type) {
        if (type == TrapType.EXPLOSIVE) return BuildActionType.EXPLOSIVE_TRAP;
        if (type == TrapType.WATER) return BuildActionType.WATER_TRAP;
        if (type == TrapType.STUN) return BuildActionType.STUN_TRAP;
        return Byte.MIN_VALUE;
    }

    public void makeGameFooter(Team winner) {
        changeState(State.IN_GAME, State.DONE);

        createEvent((builder) -> EventWrapper.createEventWrapper(builder, Event.GameFooter,
                GameFooter.createGameFooter(builder, TeamMapping.id(winner))));
    }

    /**
     * Writes events from match to one or multiple flatbuffers.
     *
     * One of the rare cases where we want a non-static inner class in Java:
     * this basically just provides a restricted interface to GameMaker.
     *
     * There is only one of these per GameMaker.
     */
    public class MatchMaker {
        private TIntArrayList robotIds;
        private TIntArrayList robotLocsX;
        private TIntArrayList robotLocsY;
        private TIntArrayList robotMoveCooldowns;
        private TIntArrayList robotActionCooldowns;
        private TIntArrayList robotHealths;
        private TIntArrayList attacksPerformed;
        private TIntArrayList attackLevels;
        private TIntArrayList buildsPerformed;
        private TIntArrayList buildLevels;
        private TIntArrayList healsPerformed;
        private TIntArrayList healLevels;

        // SpawnedBodyTable for spawnedBodies
        private TIntArrayList spawnedBodiesRobotIDs;
        private TByteArrayList spawnedBodiesTeamIDs;
        private TByteArrayList spawnedBodiesTypes;
        private TIntArrayList spawnedBodiesLocsXs; //For locs
        private TIntArrayList spawnedBodiesLocsYs; //For locs

        private TIntArrayList spawnedIds;
        private TIntArrayList diedIds; // ints

        private TIntArrayList actionIds; // ints
        private TByteArrayList actions; // Actions
        private TIntArrayList actionTargets; // ints (IDs)

        // Round statistics
        private TIntArrayList teamIDs;
        private TIntArrayList teamBreadAmounts;
        private TIntArrayList teamAComm;
        private TIntArrayList teamBComm;

        private TIntArrayList islandIDs;
        private TIntArrayList islandTurnoverTurns;
        private TIntArrayList islandOwnership;

        private TIntArrayList resourceWellLocs;
        private TIntArrayList resourceWellAdValue;
        private TIntArrayList resourceWellMnValue;
        private TIntArrayList resourceWellExValue;
        private TIntArrayList resourceWellID;
        private TIntArrayList wellAccelerationID;

        private TIntArrayList trapAddedIds;
        private TIntArrayList trapAddedX;
        private TIntArrayList trapAddedY;
        private TByteArrayList trapAddedTypes;
        private TByteArrayList trapAddedTeams;

        private TIntArrayList trapTriggeredIds;

        private TIntArrayList digLocsX;
        private TIntArrayList digLocsY;
        private TIntArrayList fillLocsX;
        private TIntArrayList fillLocsY;

        private TIntArrayList indicatorStringIDs;
        private ArrayList<String> indicatorStrings;

        // Indicator dots with locations and RGB values
        private TIntArrayList indicatorDotIDs;
        private TIntArrayList indicatorDotLocsX;
        private TIntArrayList indicatorDotLocsY;
        private TIntArrayList indicatorDotRGBsRed;
        private TIntArrayList indicatorDotRGBsGreen;
        private TIntArrayList indicatorDotRGBsBlue;

        // Indicator lines with locations and RGB values
        private TIntArrayList indicatorLineIDs;
        private TIntArrayList indicatorLineStartLocsX;
        private TIntArrayList indicatorLineStartLocsY;
        private TIntArrayList indicatorLineEndLocsX;
        private TIntArrayList indicatorLineEndLocsY;
        private TIntArrayList indicatorLineRGBsRed;
        private TIntArrayList indicatorLineRGBsGreen;
        private TIntArrayList indicatorLineRGBsBlue;

        // Robot IDs and their bytecode usage
        private TIntArrayList bytecodeIDs;
        private TIntArrayList bytecodesUsed;

        // Used to write logs.
        private final ByteArrayOutputStream logger;

        public MatchMaker() {
            this.robotIds = new TIntArrayList();
            this.robotLocsX = new TIntArrayList();
            this.robotLocsY = new TIntArrayList();
            this.robotMoveCooldowns = new TIntArrayList();
            this.robotActionCooldowns = new TIntArrayList();
            this.attacksPerformed = new TIntArrayList();
            this.attackLevels = new TIntArrayList();
            this.buildsPerformed = new TIntArrayList();
            this.buildLevels = new TIntArrayList();
            this.healsPerformed = new TIntArrayList();
            this.healLevels = new TIntArrayList();
            this.spawnedBodiesRobotIDs = new TIntArrayList();
            this.spawnedBodiesTeamIDs = new TByteArrayList();
            this.spawnedBodiesTypes = new TByteArrayList();
            this.spawnedBodiesLocsXs = new TIntArrayList();
            this.spawnedBodiesLocsYs = new TIntArrayList();
            this.spawnedIds = new TIntArrayList();
            this.diedIds = new TIntArrayList();
            this.actionIds = new TIntArrayList();
            this.actions = new TByteArrayList();
            this.actionTargets = new TIntArrayList();
            this.teamIDs = new TIntArrayList();
            this.teamBreadAmounts = new TIntArrayList();
            this.teamAComm = new TIntArrayList();
            this.teamBComm = new TIntArrayList();
            this.islandIDs = new TIntArrayList();
            this.islandTurnoverTurns = new TIntArrayList();
            this.islandOwnership = new TIntArrayList();
            this.resourceWellLocs = new TIntArrayList();
            this.resourceWellAdValue = new TIntArrayList();
            this.resourceWellMnValue = new TIntArrayList();
            this.resourceWellExValue = new TIntArrayList();
            this.resourceWellID = new TIntArrayList();
            this.wellAccelerationID = new TIntArrayList();
            this.trapAddedIds = new TIntArrayList();
            this.trapAddedX =  new TIntArrayList();
            this.trapAddedY = new TIntArrayList();
            this.trapAddedTypes = new TByteArrayList();
            this.trapAddedTeams = new TByteArrayList();
            this.trapTriggeredIds = new TIntArrayList();
            this.digLocsX = new TIntArrayList();
            this.digLocsY = new TIntArrayList();
            this.fillLocsX = new TIntArrayList();
            this.fillLocsY = new TIntArrayList();
            this.indicatorStringIDs = new TIntArrayList();
            this.indicatorStrings = new ArrayList<>();
            this.indicatorDotIDs = new TIntArrayList();
            this.indicatorDotLocsX = new TIntArrayList();
            this.indicatorDotLocsY = new TIntArrayList();
            this.indicatorDotRGBsRed = new TIntArrayList();
            this.indicatorDotRGBsBlue = new TIntArrayList();
            this.indicatorDotRGBsGreen = new TIntArrayList();
            this.indicatorLineIDs = new TIntArrayList();
            this.indicatorLineStartLocsX = new TIntArrayList();
            this.indicatorLineStartLocsY = new TIntArrayList();
            this.indicatorLineEndLocsX = new TIntArrayList();
            this.indicatorLineEndLocsY = new TIntArrayList();
            this.indicatorLineRGBsRed = new TIntArrayList();
            this.indicatorLineRGBsBlue = new TIntArrayList();
            this.indicatorLineRGBsGreen = new TIntArrayList();
            this.bytecodeIDs = new TIntArrayList();
            this.bytecodesUsed = new TIntArrayList();
            this.logger = new ByteArrayOutputStream();
        }

        public void makeMatchHeader(LiveMap gameMap) {
            changeState(State.IN_GAME, State.IN_MATCH);

            createEvent((builder) -> {
                int map = GameMapIO.Serial.serialize(builder, gameMap);

                return EventWrapper.createEventWrapper(builder, Event.MatchHeader,
                        MatchHeader.createMatchHeader(builder, map, gameMap.getRounds()));
            });

            matchHeaders.add(events.size() - 1);

            clearData();
        }

        public void makeMatchFooter(Team winTeam, int totalRounds, List<ProfilerCollection> profilerCollections) {
            changeState(State.IN_MATCH, State.IN_GAME);

            createEvent((builder) -> {
                TIntArrayList profilerFiles = new TIntArrayList();

                for (ProfilerCollection profilerCollection : profilerCollections) {
                    TIntArrayList frames = new TIntArrayList();
                    TIntArrayList profiles = new TIntArrayList();

                    for (String frame : profilerCollection.getFrames()) {
                        frames.add(builder.createString(frame));
                    }

                    for (Profiler profiler : profilerCollection.getProfilers()) {
                        TIntArrayList events = new TIntArrayList();

                        for (battlecode.instrumenter.profiler.ProfilerEvent event : profiler.getEvents()) {
                            ProfilerEvent.startProfilerEvent(builder);
                            ProfilerEvent.addIsOpen(builder, event.getType() == ProfilerEventType.OPEN);
                            ProfilerEvent.addAt(builder, event.getAt());
                            ProfilerEvent.addFrame(builder, event.getFrameId());
                            events.add(ProfilerEvent.endProfilerEvent(builder));
                        }

                        int nameOffset = builder.createString(profiler.getName());
                        int eventsOffset = ProfilerProfile.createEventsVector(builder, events.toArray());

                        ProfilerProfile.startProfilerProfile(builder);
                        ProfilerProfile.addName(builder, nameOffset);
                        ProfilerProfile.addEvents(builder, eventsOffset);
                        profiles.add(ProfilerProfile.endProfilerProfile(builder));
                    }

                    int framesOffset = ProfilerFile.createFramesVector(builder, frames.toArray());
                    int profilesOffset = ProfilerFile.createProfilesVector(builder, profiles.toArray());

                    profilerFiles.add(ProfilerFile.createProfilerFile(builder, framesOffset, profilesOffset));
                }

                int profilerFilesOffset = MatchFooter.createProfilerFilesVector(builder, profilerFiles.toArray());

                return EventWrapper.createEventWrapper(builder, Event.MatchFooter,
                    MatchFooter.createMatchFooter(builder, TeamMapping.id(winTeam), totalRounds, profilerFilesOffset));
            });

            matchFooters.add(events.size() - 1);
        }

        public void makeRound(int roundNum) {
            assertState(State.IN_MATCH);

            try {
                this.logger.flush();
            } catch (IOException e) {
                throw new RuntimeException("Can't flush byte[]outputstream?", e);
            }
            // byte[] logs = this.logger.toByteArray();
            this.logger.reset();

            createEvent((builder) -> {
                // The bodies that spawned
                int spawnedBodiesLocsP = createVecTable(builder, spawnedBodiesLocsXs, spawnedBodiesLocsYs);
                int spawnedBodiesRobotIDsP = SpawnedBodyTable.createRobotIDsVector(builder, spawnedBodiesRobotIDs.toArray());
                int spawnedBodiesTeamIDsP = SpawnedBodyTable.createTeamIDsVector(builder, spawnedBodiesTeamIDs.toArray());
                int spawnedBodiesTypesP = SpawnedBodyTable.createTypesVector(builder, spawnedBodiesTypes.toArray());
                SpawnedBodyTable.startSpawnedBodyTable(builder);
                SpawnedBodyTable.addLocs(builder, spawnedBodiesLocsP);
                SpawnedBodyTable.addRobotIDs(builder, spawnedBodiesRobotIDsP);
                SpawnedBodyTable.addTeamIDs(builder, spawnedBodiesTeamIDsP);
                SpawnedBodyTable.addTypes(builder, spawnedBodiesTypesP);
                int spawnedBodiesP = SpawnedBodyTable.endSpawnedBodyTable(builder);

                // Round statistics
                int teamIDsP = Round.createTeamIdsVector(builder, teamIDs.toArray());
                int teamBreadAmountsP = Round.createTeamResourceAmountsVector(builder, teamBreadAmounts.toArray());
                CommTable.startCommTable(builder);
                int teamACommVector = CommTable.createTeam1Vector(builder, teamAComm.toArray());
                int teamBCommVector = CommTable.createTeam2Vector(builder, teamBComm.toArray());
                CommTable.addTeam1(builder, teamACommVector);
                CommTable.addTeam2(builder, teamBCommVector);
                int teamCommunicationP = CommTable.endCommTable(builder);

                int robotIDsP = Round.createRobotIdsVector(builder, robotIds.toArray());
                int robotLocsP = createVecTable(builder, robotLocsX, robotLocsY);
                int robotMoveCooldownsP = Round.createRobotMoveCooldownsVector(builder, robotMoveCooldowns.toArray());
                int robotActionCooldownsP = Round.createRobotActionCooldownsVector(builder, robotActionCooldowns.toArray());
                int robotHealthsP = Round.createRobotHealthsVector(builder, robotHealths.toArray());
                int attacksPerformedP = Round.createAttacksPerformedVector(builder, attacksPerformed.toArray());
                int attackLevelsP = Round.createAttackLevelsVector(builder, attackLevels.toArray());
                int buildsPerformedP = Round.createBuildsPerformedVector(builder, buildsPerformed.toArray());
                int buildLevelsP = Round.createBuildLevelsVector(builder, buildLevels.toArray());
                int healsPerformedP = Round.createAttacksPerformedVector(builder, healsPerformed.toArray());
                int healLevelsP = Round.createHealLevelsVector(builder, healLevels.toArray());

                int spawnedIdsP = Round.createSpawnedIdsVector(builder, spawnedIds.toArray());
                int diedIdsP = Round.createDiedIdsVector(builder, diedIds.toArray());

                // The actions that happened
                int actionIdsP = Round.createActionIdsVector(builder, actionIds.toArray());
                int actionsP = Round.createActionsVector(builder, actions.toArray());
                int actionTargetsP = Round.createActionTargetsVector(builder, actionTargets.toArray());

                // The information about islands
                int islandIDsP = Round.createIslandIDsVector(builder, islandIDs.toArray());
                int islandTurnoverTurnsP = Round.createIslandTurnoverTurnsVector(builder, islandTurnoverTurns.toArray());
                int islandOwnershipP = Round.createIslandOwnershipVector(builder, islandOwnership.toArray());     

                // The information about wells
                int resourceWellLocsP = Round.createResourceWellLocsVector(builder, resourceWellLocs.toArray());
                int resourceWellAdValueP = Round.createWellAdamantiumValuesVector(builder, resourceWellAdValue.toArray());
                int resourceWellMnValueP = Round.createWellManaValuesVector(builder, resourceWellMnValue.toArray());
                int resourceWellExValueP = Round.createWellElixirValuesVector(builder, resourceWellExValue.toArray());
                int resourceWellIDsP = Round.createResourceIDVector(builder, resourceWellID.toArray());
                int wellAccelerationIDsP = Round.createWellAccelerationIDVector(builder, wellAccelerationID.toArray());

                int trapAddedIdsP = Round.createTrapAddedIdsVector(builder, trapAddedIds.toArray());
                int trapAddedLocsP = createVecTable(builder, trapAddedX, trapAddedY);
                int trapAddedTypesP = Round.createTrapAddedTypesVector(builder, trapAddedTypes.toArray());
                int trapAddedTeamsP = Round.createTrapAddedTeamsVector(builder, trapAddedTeams.toArray());

                int trapTriggeredIdsP = Round.createTrapTriggeredIdsVector(builder, trapTriggeredIds.toArray());

                int digLocsP = createVecTable(builder, digLocsX, digLocsY);
                int fillLocsP = createVecTable(builder, fillLocsX, fillLocsY);

                // The indicator strings that were set
                int indicatorStringIDsP = Round.createIndicatorStringIDsVector(builder, indicatorStringIDs.toArray());
                TIntArrayList indicatorStringsIntList = new TIntArrayList();
                for (String s : indicatorStrings) {
                    indicatorStringsIntList.add(builder.createString(s));
                }
                int indicatorStringsP = Round.createIndicatorStringsVector(builder, indicatorStringsIntList.toArray());

                // The indicator dots that were set
                int indicatorDotIDsP = Round.createIndicatorDotIDsVector(builder, indicatorDotIDs.toArray());
                int indicatorDotLocsP = createVecTable(builder, indicatorDotLocsX, indicatorDotLocsY);
                int indicatorDotRGBsP = createRGBTable(builder, indicatorDotRGBsRed, indicatorDotRGBsGreen, indicatorDotRGBsBlue);

                // The indicator lines that were set
                int indicatorLineIDsP = Round.createIndicatorLineIDsVector(builder, indicatorLineIDs.toArray());
                int indicatorLineStartLocsP = createVecTable(builder, indicatorLineStartLocsX, indicatorLineStartLocsY);
                int indicatorLineEndLocsP = createVecTable(builder, indicatorLineEndLocsX, indicatorLineEndLocsY);
                int indicatorLineRGBsP = createRGBTable(builder, indicatorLineRGBsRed, indicatorLineRGBsGreen, indicatorLineRGBsBlue);

                // The bytecode usage
                int bytecodeIDsP = Round.createBytecodeIDsVector(builder, bytecodeIDs.toArray());
                int bytecodesUsedP = Round.createBytecodesUsedVector(builder, bytecodesUsed.toArray());

                Round.startRound(builder);
                Round.addTeamIds(builder, teamIDsP);
                Round.addTeamCommunication(builder, teamCommunicationP);
                Round.addTeamResourceAmounts(builder, teamBreadAmountsP);
                Round.addTeamAdChanges(builder, teamAdChangesP);
                Round.addTeamMnChanges(builder, teamMnChangesP);
                Round.addTeamExChanges(builder, teamExChangesP);
                Round.addRobotIds(builder, robotIDsP);
                Round.addRobotLocs(builder, robotLocsP);
                Round.addRobotMoveCooldowns(builder, robotMoveCooldownsP);
                Round.addRobotActionCooldowns(builder, robotActionCooldownsP);
                Round.addRobotHealths(builder, robotHealthsP);
                Round.addAttacksPerformed(builder, attacksPerformedP);
                Round.addAttackLevels(builder, attackLevelsP);
                Round.addBuildsPerformed(builder, buildsPerformedP);
                Round.addBuildLevels(builder, buildLevelsP);
                Round.addHealsPerformed(builder, healsPerformedP);
                Round.addHealLevels(builder, healLevelsP);
                Round.addSpawnedBodies(builder, spawnedBodiesP);
                Round.addSpawnedIds(builder, spawnedIdsP);
                Round.addDiedIds(builder, diedIdsP);
                Round.addActionIds(builder, actionIdsP);
                Round.addActions(builder, actionsP);
                Round.addActionTargets(builder, actionTargetsP);
                Round.addIslandIDs(builder, islandIDsP);
                Round.addIslandTurnoverTurns(builder, islandTurnoverTurnsP);
                Round.addIslandOwnership(builder, islandOwnershipP);
                Round.addResourceWellLocs(builder, resourceWellLocsP);
                Round.addWellAdamantiumValues(builder, resourceWellAdValueP);
                Round.addWellManaValues(builder, resourceWellMnValueP);
                Round.addWellElixirValues(builder, resourceWellExValueP);
                Round.addResourceID(builder, resourceWellIDsP);
                Round.addWellAccelerationID(builder, wellAccelerationIDsP);
                Round.addTrapAddedIds(builder, trapAddedIdsP);
                Round.addTrapAddedLocations(builder, trapAddedLocsP);
                Round.addTrapAddedTypes(builder, trapAddedTypesP);
                Round.addTrapAddedTeams(builder, trapAddedTeamsP);
                Round.addTrapTriggeredIds(builder, trapTriggeredIdsP);
                Round.addDigLocations(builder, digLocsP);
                Round.addFillLocations(builder, fillLocsP);
                Round.addIndicatorStringIDs(builder, indicatorStringIDsP);
                Round.addIndicatorStrings(builder, indicatorStringsP);
                Round.addIndicatorDotIDs(builder, indicatorDotIDsP);
                Round.addIndicatorDotLocs(builder, indicatorDotLocsP);
                Round.addIndicatorDotRGBs(builder, indicatorDotRGBsP);
                Round.addIndicatorLineIDs(builder, indicatorLineIDsP);
                Round.addIndicatorLineStartLocs(builder, indicatorLineStartLocsP);
                Round.addIndicatorLineEndLocs(builder, indicatorLineEndLocsP);
                Round.addIndicatorLineRGBs(builder, indicatorLineRGBsP);
                Round.addRoundID(builder, roundNum);
                Round.addBytecodeIDs(builder, bytecodeIDsP);
                Round.addBytecodesUsed(builder, bytecodesUsedP);
                int round = Round.endRound(builder);
                return EventWrapper.createEventWrapper(builder, Event.Round, round);
            });

            clearData();
        }

        /**
         * @return an outputstream that will be baked into the output file
         */
        public OutputStream getOut() {
            return logger;
        }

        public void addRobot(InternalRobot robot) {
            robotIds.add(robot.getID());
            MapLocation loc = robot.getLocation();
            robotLocsX.add(loc.x);
            robotLocsY.add(loc.y);
            robotMoveCooldowns.add(robot.getMovementCooldownTurns());
            robotActionCooldowns.add(robot.getActionCooldownTurns());
            robotHealths.add(robot.getHealth());
            attacksPerformed.add(robot.getAttackExp());
            attackLevels.add(robot.getLevel(SkillType.ATTACK));
            buildsPerformed.add(robot.getBuildExp());
            buildLevels.add(robot.getLevel(SkillType.BUILD));
            healsPerformed.add(robot.getHealExp());
            healLevels.add(robot.getLevel(SkillType.HEAL));
        }

        public void addSpawned(int id) {
            spawnedIds.add(id);
        }

        public void addDied(int id) {
            diedIds.add(id);
        }

        public void addAction(int userID, byte action, int targetID) {
            actionIds.add(userID);
            actions.add(action);
            actionTargets.add(targetID);
        }

        public void addTrap(Trap trap) {
            trapAddedIds.add(trap.getId());
            MapLocation loc = trap.getLocation();
            trapAddedX.add(loc.x);
            trapAddedY.add(loc.y);
            trapAddedTypes.add(FlatHelpers.getBuildActionFromTrapType(trap.getType()));
            trapAddedTeams.add(TeamMapping.id(trap.getTeam()));
        }

        public void addTriggeredTrap(int id) {
            trapTriggeredIds.add(id);
        }

        public void addDigLocation(MapLocation loc) {
            digLocsX.add(loc.x);
            digLocsY.add(loc.y);
        }

        public void addFillLocation(MapLocation loc) {
            fillLocsX.add(loc.x);
            fillLocsY.add(loc.y);
        }

        public void addWell(Well well, int location) {
            resourceWellLocs.add(location);
            resourceWellAdValue.add(well.getResource(ResourceType.ADAMANTIUM));
            resourceWellMnValue.add(well.getResource(ResourceType.MANA));
            resourceWellExValue.add(well.getResource(ResourceType.ELIXIR));
            resourceWellID.add(well.getResourceType().resourceID);
            wellAccelerationID.add(well.accelerationId());
        }

        public void addIslandInfo(Island island) {
            islandIDs.add(island.getID());
            islandTurnoverTurns.add(island.getHealth());
            islandOwnership.add(island.getTeamInt());
        }

        public void addTeamInfo(Team team, int breadAmount, int[] sharedArray) {
            teamIDs.add(TeamMapping.id(team));
            teamBreadAmounts.add(breadAmount);
            if(team == Team.A) teamAComm = new TIntArrayList(sharedArray);
            else if(team == Team.B) teamBComm = new TIntArrayList(sharedArray);
        }

        public void addIndicatorString(int id, String string) {
            if (!showIndicators) {
                return;
            }
            indicatorStringIDs.add(id);
            indicatorStrings.add(string);
        }

        public void addIndicatorDot(int id, MapLocation loc, int red, int green, int blue) {
            if (!showIndicators) {
                return;
            }
            indicatorDotIDs.add(id);
            indicatorDotLocsX.add(loc.x);
            indicatorDotLocsY.add(loc.y);
            indicatorDotRGBsRed.add(red);
            indicatorDotRGBsGreen.add(green);
            indicatorDotRGBsBlue.add(blue);
        }

        public void addIndicatorLine(int id, MapLocation startLoc, MapLocation endLoc, int red, int green, int blue) {
            if (!showIndicators) {
                return;
            }
            indicatorLineIDs.add(id);
            indicatorLineStartLocsX.add(startLoc.x);
            indicatorLineStartLocsY.add(startLoc.y);
            indicatorLineEndLocsX.add(endLoc.x);
            indicatorLineEndLocsY.add(endLoc.y);
            indicatorLineRGBsRed.add(red);
            indicatorLineRGBsGreen.add(green);
            indicatorLineRGBsBlue.add(blue);
        }

        public void addBytecodes(int id, int bytecodes) {
            bytecodeIDs.add(id);
            bytecodesUsed.add(bytecodes);
        }

        public void addSpawnedRobot(InternalRobot robot) {
            spawnedBodiesRobotIDs.add(robot.getID());
            spawnedBodiesLocsXs.add(robot.getLocation().x);
            spawnedBodiesLocsYs.add(robot.getLocation().y);
            spawnedBodiesTeamIDs.add(TeamMapping.id(robot.getTeam()));
            spawnedBodiesTypes.add(FlatHelpers.getBodyTypeFromRobotType(robot.getType()));
        }

        private void clearData() {
            robotIds.clear();
            robotLocsX.clear();
            robotLocsY.clear();
            robotMoveCooldowns.clear();
            robotActionCooldowns.clear();
            attacksPerformed.clear();
            attackLevels.clear();
            buildsPerformed.clear();
            buildLevels.clear();
            healsPerformed.clear();
            healLevels.clear();
            spawnedBodiesRobotIDs.clear();
            spawnedBodiesTeamIDs.clear();
            spawnedBodiesTypes.clear();
            spawnedBodiesLocsXs.clear();
            spawnedBodiesLocsYs.clear();
            spawnedIds.clear();
            diedIds.clear();
            actionIds.clear();
            actions.clear();
            actionTargets.clear();
            teamIDs.clear();
            teamBreadAmounts.clear();
            teamAComm.clear();
            teamBComm.clear();
            islandIDs.clear();
            islandTurnoverTurns.clear();
            islandOwnership.clear();
            resourceWellLocs.clear();
            resourceWellAdValue.clear();
            resourceWellMnValue.clear();
            resourceWellExValue.clear();
            resourceWellID.clear();
            wellAccelerationID.clear();
            trapAddedIds.clear();
            trapAddedX.clear();
            trapAddedY.clear();
            trapAddedTypes.clear();
            trapAddedTeams.clear();
            trapTriggeredIds.clear();
            digLocsX.clear();
            digLocsY.clear();
            fillLocsX.clear();
            fillLocsY.clear();
            indicatorStringIDs.clear();
            indicatorStrings.clear();
            indicatorDotIDs.clear();
            indicatorDotLocsX.clear();
            indicatorDotLocsY.clear();
            indicatorDotRGBsRed.clear();
            indicatorDotRGBsBlue.clear();
            indicatorDotRGBsGreen.clear();
            indicatorLineIDs.clear();
            indicatorLineStartLocsX.clear();
            indicatorLineStartLocsY.clear();
            indicatorLineEndLocsX.clear();
            indicatorLineEndLocsY.clear();
            indicatorLineRGBsRed.clear();
            indicatorLineRGBsBlue.clear();
            indicatorLineRGBsGreen.clear();
            bytecodeIDs.clear();
            bytecodesUsed.clear();
        }
    }
}
