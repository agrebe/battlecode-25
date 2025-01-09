package attacc;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public class RobotPlayer {
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
        Direction.NORTH,
        Direction.NORTHEAST,
        Direction.EAST,
        Direction.SOUTHEAST,
        Direction.SOUTH,
        Direction.SOUTHWEST,
        Direction.WEST,
        Direction.NORTHWEST,
    };

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm alive");

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the UnitType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()){
                    case SOLDIER: runSoldier(rc); break; 
                    case MOPPER: runMopper(rc); break;
                    case SPLASHER: break; // Consider upgrading examplefuncsplayer to use splashers!
                    default: runTower(rc); break;
                    }
                }
             catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println("GameActionException");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println("Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for towers.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runTower(RobotController rc) throws GameActionException{
        // don't build if we have < 200 paint (since otherwise we will never build soldiers)
        if (rc.getPaint() < 200) return;
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Pick a random robot type to build.
        int robotType = rng.nextInt(3);
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
        }
        else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc)){
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
        }
        else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            // rc.buildRobot(UnitType.SPLASHER, nextLoc);
            // System.out.println("BUILT A SPLASHER");
            rc.setIndicatorString("SPLASHER NOT IMPLEMENTED YET");
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

        // TODO: can we attack other bots?
    }


    /**
     * Run a single turn for a Soldier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSoldier(RobotController rc) throws GameActionException{
      MapLocation me = rc.getLocation();
      Team myTeam = rc.getTeam();
      Team enemyTeam = myTeam.opponent();
        // Sense information about all visible nearby tiles.
        MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();
        // Search for a nearby ruin to complete.
        MapLocation curRuin = null;
        for (MapLocation ruinLoc : rc.senseNearbyRuins(-1)) {
          // upgrade towers if we have huge amounts of gold
          if (rc.getMoney() > 6000 && rc.senseNearbyRobots(ruinLoc, 0, myTeam).length == 1)
            if (rc.canUpgradeTower(ruinLoc))
              rc.upgradeTower(ruinLoc);
          // ignore those that already have a tower
          if ((rc.senseNearbyRobots(ruinLoc, 0, myTeam).length == 0) && (rc.senseNearbyRobots(ruinLoc, 0, enemyTeam).length == 0)) {
            // see if this is closer than curRuin
            if (curRuin == null || curRuin.distanceSquaredTo(me) > ruinLoc.distanceSquaredTo(me))
              curRuin = ruinLoc;
          }
        }
        if (curRuin != null){
            Direction dir = rc.getLocation().directionTo(curRuin);
            // Mark the pattern we need to draw to build a tower here if we haven't already.
            MapLocation shouldBeMarked = curRuin.subtract(dir);
            // randomly choose paint or money tower
            UnitType tower = UnitType.LEVEL_ONE_PAINT_TOWER;
            if (rng.nextInt(2) == 1) tower = UnitType.LEVEL_ONE_MONEY_TOWER;
            if (rc.senseMapInfo(shouldBeMarked).getMark() == PaintType.EMPTY && rc.canMarkTowerPattern(tower, curRuin)){
                rc.markTowerPattern(tower, curRuin);
                System.out.println("Trying to build a tower at " + curRuin);
            }
            // Fill in any spots in the pattern with the appropriate paint.
            for (MapInfo patternTile : rc.senseNearbyMapInfos(curRuin, 8)){
                if (patternTile.getMark() != patternTile.getPaint() && patternTile.getMark() != PaintType.EMPTY){
                    boolean useSecondaryColor = patternTile.getMark() == PaintType.ALLY_SECONDARY;
                    // Don't bother trying to paint over enemy paint -- this doesn't work
                    if (patternTile.getPaint() == PaintType.ENEMY_PRIMARY || patternTile.getPaint() == PaintType.ENEMY_SECONDARY)
                        continue;
                    if (rc.canAttack(patternTile.getMapLocation()))
                        rc.attack(patternTile.getMapLocation(), useSecondaryColor);
                }
            }
            // Complete the ruin if we can.
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, curRuin)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_MONEY_TOWER, curRuin);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + curRuin + "!");
            }
            if (rc.canCompleteTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, curRuin)){
                rc.completeTowerPattern(UnitType.LEVEL_ONE_PAINT_TOWER, curRuin);
                rc.setTimelineMarker("Tower built", 0, 255, 0);
                System.out.println("Built a tower at " + curRuin + "!");
            }
            // try to move toward ruin if more than sqrt(8) away
            if (me.distanceSquaredTo(curRuin) > 8 && rc.canMove(dir)) rc.move(dir);
        }
//        // try to move in a random location
//        {
//          Direction dir = directions[rng.nextInt(directions.length)];
//          // only move if paint color is friendly
//          if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
//              rc.move(dir);
//        }

        // Move and attack randomly if no objective.
        {
          Direction dir = directions[rng.nextInt(directions.length)];
          MapLocation nextLoc = rc.getLocation().add(dir);
          if (rc.canMove(dir)){
              rc.move(dir);
          }
        }
        // Try to paint beneath us as we walk to avoid paint penalties.
        // Avoiding wasting paint by re-painting our own tiles.
        MapInfo currentTile = rc.senseMapInfo(rc.getLocation());
        if (currentTile.getPaint() == PaintType.EMPTY && rc.canAttack(rc.getLocation())){
            rc.attack(rc.getLocation());
        } else {
          // try to paint adjacent tile
          for (Direction dir : directions) {
            MapLocation newLoc = rc.getLocation().add(dir);
            if (!rc.canSenseLocation(newLoc)) continue;
            MapInfo newTile = rc.senseMapInfo(newLoc);
            if (rc.canAttack(newLoc) && newTile.getPaint() == PaintType.EMPTY)
              rc.attack(newLoc);
          }
          /*
          // try to paint farther-away tiles
          for (MapInfo info : rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared))
            if (info.getPaint() == PaintType.EMPTY && rc.canAttack(info.getMapLocation())) {
              rc.attack(info.getMapLocation());
              break;
            }
          */
        }
    }


    /**
     * Run a single turn for a Mopper.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runMopper(RobotController rc) throws GameActionException{
      MapLocation me = rc.getLocation();
      // find nearest enemy robot and move toward them (but not onto enemy color)
      MapLocation closestEnemy = null;
      for (RobotInfo enemy : rc.senseNearbyRobots(-1, rc.getTeam().opponent()))
        if (closestEnemy == null || enemy.location.distanceSquaredTo(me) < closestEnemy.distanceSquaredTo(me))
          closestEnemy = enemy.location;
      if (closestEnemy == null) {
        // set closest enemy to be enemy paint rather than enemy robot
        for (MapInfo enemy : rc.senseNearbyMapInfos())
          if ((closestEnemy == null || enemy.getMapLocation().distanceSquaredTo(me) < closestEnemy.distanceSquaredTo(me))
              && enemy.getPaint().isEnemy())
            closestEnemy = enemy.getMapLocation();
      }
      if (closestEnemy != null) {
        // try to move toward closest enemy without walking off paint
        Direction dir = me.directionTo(closestEnemy);
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
            rc.move(dir);
      }

        // Move and attack randomly.
        // Don't move to squares unless they match our paint color
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly()){
            rc.move(dir);
        }
        // find location with enemy paint and/or robot to attack
        // first try to attack
        Team enemy = rc.getTeam().opponent();
        for (Direction newdir : directions) {
          MapLocation nextLoc = rc.getLocation().add(newdir);
          if (!rc.canSenseLocation(nextLoc)) continue;
          if (!rc.canAttack(nextLoc)) continue;
          if (rc.senseNearbyRobots(nextLoc, 0, enemy).length > 0) {
            rc.attack(nextLoc);
            rc.setIndicatorString("Attacking enemy robot");
            return;
          }
        }
        // then try to remove enemy paint
        for (Direction newdir : directions) {
          MapLocation nextLoc = rc.getLocation().add(newdir);
          if (!rc.canSenseLocation(nextLoc)) continue;
          if (!rc.canAttack(nextLoc)) continue;
          MapInfo nextTile = rc.senseMapInfo(nextLoc);
          PaintType paint = nextTile.getPaint();
          boolean isEnemyPaint = (paint == PaintType.ENEMY_PRIMARY) || (paint == PaintType.ENEMY_SECONDARY);
          if (isEnemyPaint) {
            rc.attack(nextLoc);
            rc.setIndicatorString("Removing paint from " + nextLoc);
          }
        }
        // We can also move our code into different methods or classes to better organize it!
        updateEnemyRobots(rc);
    }

    public static void updateEnemyRobots(RobotController rc) throws GameActionException{
        // Sensing methods can be passed in a radius of -1 to automatically 
        // use the largest possible value.
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        if (enemyRobots.length != 0){
            //rc.setIndicatorString("There are nearby enemy robots! Scary!");
            // Save an array of locations with enemy robots in them for possible future use.
            MapLocation[] enemyLocations = new MapLocation[enemyRobots.length];
            for (int i = 0; i < enemyRobots.length; i++){
                enemyLocations[i] = enemyRobots[i].getLocation();
            }
            RobotInfo[] allyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
            // Occasionally try to tell nearby allies how many enemy robots we see.
            if (rc.getRoundNum() % 20 == 0){
                for (RobotInfo ally : allyRobots){
                    if (rc.canSendMessage(ally.location, enemyRobots.length)){
                        rc.sendMessage(ally.location, enemyRobots.length);
                    }
                }
            }
        }
    }
}
