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
    // direction from initial location to center
    static Direction towardCenter = null;
    static boolean producedUnit = false;

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
    public static boolean isTower(UnitType t) throws GameActionException {
      return (t == UnitType.LEVEL_ONE_PAINT_TOWER || t == UnitType.LEVEL_TWO_PAINT_TOWER || t == UnitType.LEVEL_THREE_PAINT_TOWER
              || t == UnitType.LEVEL_ONE_MONEY_TOWER || t == UnitType.LEVEL_TWO_MONEY_TOWER || t == UnitType.LEVEL_THREE_MONEY_TOWER
              || t == UnitType.LEVEL_ONE_DEFENSE_TOWER || t == UnitType.LEVEL_TWO_DEFENSE_TOWER || t == UnitType.LEVEL_THREE_DEFENSE_TOWER);
    }
    public static boolean isDefenseTower(UnitType t) throws GameActionException {
      return (t == UnitType.LEVEL_ONE_DEFENSE_TOWER || t == UnitType.LEVEL_TWO_DEFENSE_TOWER || t == UnitType.LEVEL_THREE_DEFENSE_TOWER);
    }

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

        // set initial direction
        MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        towardCenter = rc.getLocation().directionTo(center);

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
                    case SPLASHER: runSplasher(rc); break;
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
        // TODO: can we attack other bots?
        // start with an AOE attack
        if (rc.canAttack(null)) rc.attack(null);
        // find an enemy unit in range and attack that
        // ideally attack min-health enemy
        RobotInfo bestTarget = null;
        for (RobotInfo enemy : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent()))
          if (rc.canAttack(enemy.location) && (bestTarget == null || bestTarget.health > enemy.health))
            bestTarget = enemy;
        if (bestTarget != null) 
          rc.attack(bestTarget.location);

        // don't build if we have < 200 paint (since otherwise we will never build soldiers)
        if (rc.getPaint() < 200 && (rc.getType() != UnitType.LEVEL_ONE_MONEY_TOWER)) return;
        // increase threshold to 300 on round 200 to build splashers
        if (rc.getPaint() < 300 && rc.getRoundNum() > 200 && (rc.getType() != UnitType.LEVEL_ONE_MONEY_TOWER)) return;
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // Pick a random robot type to build.
        int robotTypes = 2;
        if (rc.getRoundNum() > 200 && rng.nextInt(3) == 0) robotTypes ++; // activate splashers (but 1/3 as likely as others)
        int robotType = rng.nextInt(robotTypes);
        if (robotType == 0 && rc.canBuildRobot(UnitType.SOLDIER, nextLoc)){
            rc.buildRobot(UnitType.SOLDIER, nextLoc);
            System.out.println("BUILT A SOLDIER");
            producedUnit = true;
        }
        // build mopper (but only on friendly territory)
        else if (robotType == 1 && rc.canBuildRobot(UnitType.MOPPER, nextLoc) && rc.senseMapInfo(nextLoc).getPaint().isAlly()){
            rc.buildRobot(UnitType.MOPPER, nextLoc);
            System.out.println("BUILT A MOPPER");
            producedUnit = true;
        }
        else if (robotType == 2 && rc.canBuildRobot(UnitType.SPLASHER, nextLoc)){
            rc.buildRobot(UnitType.SPLASHER, nextLoc);
            System.out.println("BUILT A SPLASHER");
            producedUnit = true;
        }

        // Read incoming messages
        Message[] messages = rc.readMessages(-1);
        for (Message m : messages) {
            System.out.println("Tower received message: '#" + m.getSenderID() + " " + m.getBytes());
        }

    }

    /**
     * Run a single turn for a Splasher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    public static void runSplasher(RobotController rc) throws GameActionException{
      MapLocation me = rc.getLocation();
      Team myTeam = rc.getTeam();
      Team enemyTeam = myTeam.opponent();
      // Sense information about all visible nearby tiles.
      MapInfo[] nearbyTiles = rc.senseNearbyMapInfos();

      // run away from nearby moppers
      for (RobotInfo enemy : rc.senseNearbyRobots(9, enemyTeam)) {
        if (enemy.type == UnitType.MOPPER) {
          Direction dir = enemy.location.directionTo(me);
          if (rc.canMove(dir)) rc.move(dir);
        }
      }
      // move toward enemy painted tiles
      MapLocation closestEnemy = null;
      if (rc.isActionReady() && closestEnemy == null) {
        // set closest enemy to be enemy paint rather than enemy robot
        for (MapInfo enemy : nearbyTiles)
          if ((closestEnemy == null || enemy.getMapLocation().distanceSquaredTo(me) < closestEnemy.distanceSquaredTo(me))
              && enemy.getPaint().isEnemy())
            closestEnemy = enemy.getMapLocation();
      }
      if (closestEnemy != null) {
        Direction dir = me.directionTo(closestEnemy);
        if (rc.canMove(dir)) rc.move(dir);
      }

      // Move and attack randomly if no objective.
      {
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation nextLoc = rc.getLocation().add(dir);
        // don't move onto enemy paint
        if (rc.canMove(dir) && !rc.senseMapInfo(nextLoc).getPaint().isEnemy()){
            rc.move(dir);
        }
        // bias toward moving toward center of map
        dir = towardCenter;
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
            rc.move(dir);
      }
      if (!rc.isActionReady()) return;
      // paint over enemy tiles
      // evaluation of goodness of attack
      MapLocation bestTarget = null;
      int bestScore = 10; // minimum score -- anything less is waste of paint
      for (MapLocation target : rc.getAllLocationsWithinRadiusSquared(me, rc.getType().actionRadiusSquared)) {
        int score = 0;
        // locations that don't have ally paint
        for (MapInfo nearbyLoc : rc.senseNearbyMapInfos(target, 4))
          if (nearbyLoc.isPassable() && !nearbyLoc.getPaint().isAlly()) score += 1;
        for (MapInfo nearbyLoc : rc.senseNearbyMapInfos(target, 2))
          if (nearbyLoc.isPassable() && nearbyLoc.getPaint().isEnemy()) score += 1;
        if (score > bestScore) {
          bestScore = score;
          bestTarget = target;
        }
      }
      if (bestTarget != null && rc.canAttack(bestTarget)) rc.attack(bestTarget);
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
          if (rc.getMoney() > 3000 && rc.senseNearbyRobots(ruinLoc, 0, myTeam).length == 1)
            if (rc.canUpgradeTower(ruinLoc))
              rc.upgradeTower(ruinLoc);
          // ignore those that already have a tower
          if ((rc.senseNearbyRobots(ruinLoc, 0, myTeam).length == 0) && (rc.senseNearbyRobots(ruinLoc, 0, enemyTeam).length == 0)
              && rc.getNumberTowers() < 25) {
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
            // 60% of towers are money initially
            if (rng.nextInt(10) >= 4) tower = UnitType.LEVEL_ONE_MONEY_TOWER;
            // prioritize paint towers if we have > 5000 gold
            if (rc.getMoney() > 5000) tower = UnitType.LEVEL_THREE_PAINT_TOWER;
            // start with money tower
            if (rc.getNumberTowers() < 3) tower = UnitType.LEVEL_ONE_MONEY_TOWER;
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
            if (rc.getMoney() > 1000) {
              // try to move toward ruin if more than sqrt(3) away
              if (me.distanceSquaredTo(curRuin) > 3 && rc.canMove(dir)) rc.move(dir);
              // also try to revolve around tower
              else if (rc.canMove(dir.rotateRight())) rc.move(dir.rotateRight());
              else if (rc.canMove(dir.rotateLeft())) rc.move(dir.rotateRight());
            }
        }
        // highest attack priority -- attack a tower
        for (RobotInfo enemy : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, enemyTeam)) {
          MapLocation enemyLoc = enemy.location;
          if (rc.canSenseLocation(enemyLoc) && rc.senseMapInfo(enemyLoc).hasRuin())
            if (rc.canAttack(enemyLoc)) rc.attack(enemyLoc);
        }
//        // try to move in a random location
//        {
//          Direction dir = directions[rng.nextInt(directions.length)];
//          // only move if paint color is friendly
//          if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
//              rc.move(dir);
//        }

        // run away from nearby moppers
        for (RobotInfo enemy : rc.senseNearbyRobots(9, enemyTeam)) {
          if (enemy.type == UnitType.MOPPER) {
            Direction dir = enemy.location.directionTo(me);
            if (rc.canMove(dir)) rc.move(dir);
          }
        }
        // Move and attack randomly if no objective.
        {
          Direction dir = directions[rng.nextInt(directions.length)];
          MapLocation nextLoc = rc.getLocation().add(dir);
          // don't move onto enemy paint
          if (rc.canMove(dir) && !rc.senseMapInfo(nextLoc).getPaint().isEnemy()){
              rc.move(dir);
          }
          // bias toward moving toward center of map
          dir = towardCenter;
          if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
              rc.move(dir);
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
          // try to paint farther-away tiles where friends are standing
          for (MapInfo info : rc.senseNearbyMapInfos(UnitType.SOLDIER.actionRadiusSquared))
            if (info.getPaint() == PaintType.EMPTY && rc.canAttack(info.getMapLocation())
                && rc.getRoundNum() > 1800) {
              rc.attack(info.getMapLocation());
              break;
            }
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
        if (closestEnemy == null || enemy.location.distanceSquaredTo(me) < closestEnemy.distanceSquaredTo(me)
            && enemy.paintAmount > 0)
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
        // if we're already in range, attack first and then walk away
        if (me.distanceSquaredTo(closestEnemy) <= 2) {
          if (rc.canAttack(closestEnemy)) rc.attack(closestEnemy);
          if (rc.canMove(dir.opposite())) rc.move(dir.opposite());
        }
        // if closest enemy is a tower, run away from it rather than towards it
        RobotInfo enemyRobot = rc.senseRobotAtLocation(closestEnemy);
        // if this is not null (enemy is robot and not paint), check if it's a tower
        if (enemyRobot != null) {
          UnitType enemyType = enemyRobot.getType();
          if (enemyType != UnitType.MOPPER && enemyType != UnitType.SOLDIER && enemyType != UnitType.MOPPER) {
            dir = dir.opposite();
          }
        }
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
            rc.move(dir);
        // can move into non-ally paint if necessary for attack
        // only do this if attacking unit (not paint)
        if (rc.canMove(dir) && me.distanceSquaredTo(closestEnemy) >= 4 && me.distanceSquaredTo(closestEnemy) <= 8 && rc.isLocationOccupied(closestEnemy)
            && rc.getActionCooldownTurns() == 0 && rc.senseNearbyRobots(-1, rc.getTeam()).length > rc.senseNearbyRobots(-1, rc.getTeam().opponent()).length * 2)
          rc.move(dir);
      }

        // Move and attack randomly.
        // Don't move to squares unless they match our paint color
        Direction dir = directions[rng.nextInt(directions.length)];
        if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly()){
            rc.move(dir);
        } else {
          // bias toward moving toward center of map
          //MapLocation center = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
          dir = towardCenter;
          if (rc.canMove(dir) && rc.senseMapInfo(rc.getLocation().add(dir)).getPaint().isAlly())
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
            // only attack robots wtih paint
            if (rc.senseRobotAtLocation(nextLoc).paintAmount > 0) {
              rc.attack(nextLoc);
              rc.setIndicatorString("Attacking enemy robot");
              return;
            }
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
        // if we can't attack, then transfer paint
        // transfer paint if we have more than 67% paint and target has <50%
        /*
        if (rc.getPaint() > rc.getType().paintCapacity / 2)
          for (RobotInfo friend : rc.senseNearbyRobots(2, rc.getTeam()))
            if (friend.paintAmount < friend.type.paintCapacity / 2 && friend.type == UnitType.SOLDIER) {
              int transferAmount = Math.min(rc.getPaint() - rc.getType().paintCapacity / 2,
                                            friend.type.paintCapacity / 2 - friend.paintAmount);
              if (transferAmount > 10 && rc.canTransferPaint(friend.location, transferAmount))
                rc.transferPaint(friend.location, transferAmount);
            }
        */
              
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
