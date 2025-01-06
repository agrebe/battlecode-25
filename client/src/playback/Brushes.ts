import { schema } from 'battlecode-schema'
import {
    MapEditorBrush,
    MapEditorBrushField,
    MapEditorBrushFieldType,
    SymmetricMapEditorBrush
} from '../components/sidebar/map-editor/MapEditorBrush'
import Bodies from './Bodies'
import { CurrentMap, StaticMap } from './Map'
import { Vector } from './Vector'
import { Team } from './Game'

const applyInRadius = (
    map: CurrentMap | StaticMap,
    x: number,
    y: number,
    radius: number,
    func: (idx: number) => void
) => {
    for (let i = -radius; i <= radius; i++) {
        for (let j = -radius; j <= radius; j++) {
            if (Math.sqrt(i * i + j * j) <= radius) {
                const target_x = x + i
                const target_y = y + j
                if (target_x >= 0 && target_x < map.width && target_y >= 0 && target_y < map.height) {
                    const target_idx = map.locationToIndex(target_x, target_y)
                    func(target_idx)
                }
            }
        }
    }
}

const squareIntersects = (check: Vector, center: Vector, radius: number) => {
    return (
        check.x >= center.x - radius &&
        check.x <= center.x + radius &&
        check.y >= center.y - radius &&
        check.y <= center.y + radius
    )
}

export class WallsBrush extends SymmetricMapEditorBrush<StaticMap> {
    public readonly name = 'Walls'
    public readonly fields = {
        shouldAdd: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        radius: {
            type: MapEditorBrushFieldType.POSITIVE_INTEGER,
            value: 1,
            label: 'Radius'
        }
    }

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const add = (idx: number) => {
            // Check if this is a valid wall location
            const pos = this.map.indexToLocation(idx)
            const ruin = this.map.ruins.findIndex((l) => squareIntersects(l, pos, 2))
            const paint = this.map.initialPaint[idx]
            if (ruin !== -1 || paint) return true
            this.map.walls[idx] = 1
        }

        const remove = (idx: number) => {
            this.map.walls[idx] = 0
        }

        const radius: number = fields.radius.value - 1
        const changes: { idx: number; prevValue: number }[] = []
        applyInRadius(this.map, x, y, radius, (idx) => {
            const prevValue = this.map.walls[idx]
            if (fields.shouldAdd.value) {
                if (add(idx)) return
                changes.push({ idx, prevValue })
            } else {
                remove(idx)
                changes.push({ idx, prevValue })
            }
        })

        return () => {
            changes.forEach(({ idx, prevValue }) => {
                this.map.walls[idx] = prevValue
            })
        }
    }
}

export class RuinsBrush extends SymmetricMapEditorBrush<StaticMap> {
    public readonly name = 'Ruins'
    public readonly fields = {
        shouldAdd: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        }
    }

    constructor(map: StaticMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>) {
        const add = (x: number, y: number) => {
            // Check if ruin is too close to the border
            if (x <= 1 || x >= this.map.width - 2 || y <= 1 || y >= this.map.height - 2) {
                return true
            }

            // Check if this is a valid ruin location
            const pos = { x, y }
            const idx = this.map.locationToIndex(x, y)
            const ruin = this.map.ruins.findIndex((l) => squareIntersects(l, pos, 4))
            const wall = this.map.walls.findIndex(
                (v, i) => !!v && squareIntersects(this.map.indexToLocation(i), pos, 2)
            )
            const paint = this.map.initialPaint[idx]
            if (ruin !== -1 || wall !== -1 || paint) {
                return true
            }

            this.map.ruins.push({ x, y })
        }

        const remove = (x: number, y: number) => {
            const foundIdx = this.map.ruins.findIndex((l) => l.x === x && l.y === y)
            if (foundIdx === -1) return true
            this.map.ruins.splice(foundIdx, 1)
        }

        if (fields.shouldAdd.value) {
            if (add(x, y)) return null
            return () => remove(x, y)
        } else {
            if (remove(x, y)) return null
            return () => add(x, y)
        }
    }
}

export class PaintBrush extends SymmetricMapEditorBrush<CurrentMap> {
    public readonly name = 'Paint'
    public readonly fields = {
        shouldAdd: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        team: {
            type: MapEditorBrushFieldType.TEAM,
            value: 0
        },
        radius: {
            type: MapEditorBrushFieldType.POSITIVE_INTEGER,
            value: 1,
            label: 'Radius'
        },
        paintType: {
            type: MapEditorBrushFieldType.SINGLE_SELECT,
            value: 0,
            label: 'Paint Type',
            options: [
                { value: 0, label: 'Primary' },
                { value: 1, label: 'Secondary' }
            ]
        }
    }

    constructor(map: CurrentMap) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>, robotOne: boolean) {
        const add = (idx: number, value: number) => {
            // Check if this is a valid paint location
            const pos = this.map.indexToLocation(idx)
            const ruin = this.map.staticMap.ruins.find((r) => r.x === pos.x && r.y === pos.y)
            const wall = this.map.staticMap.walls[idx]
            if (ruin || wall) return true
            this.map.paint[idx] = value
            this.map.staticMap.initialPaint[idx] = this.map.paint[idx]
        }

        const remove = (idx: number) => {
            this.map.paint[idx] = 0
            this.map.staticMap.initialPaint[idx] = 0
        }

        const radius: number = fields.radius.value - 1
        const changes: { idx: number; prevPaint: number }[] = []
        applyInRadius(this.map, x, y, radius, (idx) => {
            const prevPaint = this.map.paint[idx]
            if (fields.shouldAdd.value) {
                let teamIdx = robotOne ? 0 : 1
                if (fields.team.value === 1) teamIdx = 1 - teamIdx
                const newVal = teamIdx * 2 + 1 + fields.paintType.value
                if (add(idx, newVal)) return
                changes.push({ idx, prevPaint })
            } else {
                remove(idx)
                changes.push({ idx, prevPaint })
            }
        })

        return () => {
            changes.forEach(({ idx, prevPaint }) => {
                this.map.paint[idx] = prevPaint
                this.map.staticMap.initialPaint[idx] = prevPaint
            })
        }
    }
}

export class TowerBrush extends SymmetricMapEditorBrush<StaticMap> {
    public readonly name = 'Towers'
    public readonly fields = {
        isTower: {
            type: MapEditorBrushFieldType.ADD_REMOVE,
            value: true
        },
        team: {
            type: MapEditorBrushFieldType.TEAM,
            value: 0
        },
        towerType: {
            type: MapEditorBrushFieldType.SINGLE_SELECT,
            value: schema.RobotType.PAINT_TOWER,
            label: 'Tower Type',
            options: [
                { value: schema.RobotType.PAINT_TOWER, label: 'Paint Tower' },
                { value: schema.RobotType.MONEY_TOWER, label: 'Money Tower' }
            ]
        }
    }

    constructor(
        private readonly bodies: Bodies,
        map: StaticMap
    ) {
        super(map)
    }

    public symmetricApply(x: number, y: number, fields: Record<string, MapEditorBrushField>, robotOne: boolean) {
        const towerType: schema.RobotType = fields.towerType.value
        const isTower: boolean = fields.isTower.value

        const add = (x: number, y: number, team: Team) => {
            // Check if this is a valid tower location
            const pos = { x, y }
            const idx = this.map.locationToIndex(x, y)
            const body = this.bodies.getBodyAtLocation(x, y)
            const wall = this.map.walls[idx]
            const ruin = this.map.ruins.findIndex((l) => squareIntersects(l, pos, 2))

            if (body || wall || ruin !== -1) return null

            const id = this.bodies.getNextID()
            this.bodies.spawnBodyFromValues(id, towerType, team, pos)

            return id
        }

        const remove = (x: number, y: number) => {
            const body = this.bodies.getBodyAtLocation(x, y)

            if (!body) return null

            const team = body.team
            this.bodies.removeBody(body.id)

            return team
        }

        if (isTower) {
            let teamIdx = robotOne ? 0 : 1
            if (fields.team.value === 1) teamIdx = 1 - teamIdx
            const team = this.bodies.game.teams[teamIdx]
            const id = add(x, y, team)
            if (id) return () => this.bodies.removeBody(id)
            return null
        } else {
            const team = remove(x, y)
            if (!team) return null
            return () => add(x, y, team)
        }
    }
}
