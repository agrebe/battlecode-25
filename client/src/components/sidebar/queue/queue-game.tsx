import React, { useState } from 'react'
import Game from '../../../playback/Game'
import { useAppContext } from '../../../app-context'
import { IconContext } from 'react-icons'
import { IoCloseCircle, IoCloseCircleOutline } from 'react-icons/io5'
import { schema } from 'battlecode-schema'
import GameRunner from '../../../playback/GameRunner'
import { useMatch } from '../../../playback/GameRunner'

interface Props {
    game: Game
}

export const QueuedGame: React.FC<Props> = (props) => {
    const context = useAppContext()
    const activeMatch = useMatch()
    const isTournamentMode = context.state.tournament !== undefined
    const [hoveredClose, setHoveredClose] = useState(false)

    const close = () => {
        context.setState((prevState) => ({
            ...prevState,
            queue: context.state.queue.filter((v) => v !== props.game)
        }))

        if (GameRunner.game === props.game) GameRunner.setGame(undefined)
    }

    const getWinText = (winType: schema.WinType) => {
        switch (winType) {
            case schema.WinType.RESIGNATION:
                return 'by resignation '
            case schema.WinType.MAJORITY_PAINTED:
                return 'by having paint majority '
            case schema.WinType.ALL_UNITS_DESTROYED:
                return 'by destroying all enemy units '
            case schema.WinType.AREA_PAINTED:
                return 'by painting more territory '
            case schema.WinType.MORE_TOWERS:
                return 'with more towers alive '
            case schema.WinType.MORE_MONEY:
                return 'with more money '
            case schema.WinType.MORE_STORED_PAINT:
                return 'with more stored paint '
            case schema.WinType.MORE_ROBOTS:
                return 'with more robots alive '
            case schema.WinType.COIN_FLIP:
                return 'by coin flip '
            default:
                return ''
        }
    }

    return (
        <div
            className={`relative mr-auto rounded-md bg-lightCard border mb-4 p-3 w-[96%] shadow-md ${
                props.game.matches.includes(activeMatch!) ? 'border-red' : 'border-gray-500'
            }`}
        >
            <div className="text-xs whitespace mb-2 overflow-ellipsis overflow-hidden">
                <span className="font-bold text-team0" style={{ textShadow: '-0.5px 0.5px 1px black' }}>
                    {props.game.teams[0].name}
                </span>
                <span className="mx-1.5">vs</span>
                <span className="font-bold text-team1" style={{ textShadow: '-0.5px 0.5px 1px black' }}>
                    {props.game.teams[1].name}
                </span>
            </div>
            {props.game.matches.map((match, i) => (
                <p
                    key={i}
                    className={
                        'leading-4 rounded-sm border my-1.5 py-1 px-2 ' +
                        'bg-light hover:bg-lightHighlight cursor-pointer ' +
                        (activeMatch === match
                            ? 'bg-lightHighlight hover:bg-medHighlight border-red'
                            : 'border-gray-500')
                    }
                    onClick={() => GameRunner.setMatch(match)}
                >
                    <span className="text-xxs font-bold">{match.map.name}</span>
                    {!isTournamentMode && (
                        <span className="text-xxs leading-tight">
                            <span className="mx-1">-</span>
                            {match.winner !== null && match.winType !== null ? (
                                <>
                                    <span
                                        style={{ textShadow: '-0.5px 0.5px 1px black' }}
                                        className={`font-bold text-team${match.winner.id - 1}`}
                                    >
                                        {match.winner.name}
                                    </span>
                                    <span>{` wins ${getWinText(match.winType)}after ${match.maxRound} rounds`}</span>
                                </>
                            ) : (
                                <span>Winner not known</span>
                            )}
                        </span>
                    )}
                </p>
            ))}
            <div
                className="absolute -right-3 -top-3 w-6 h-6 cursor-pointer rounded-full bg-white"
                onClick={() => close()}
                onMouseEnter={() => setHoveredClose(true)}
                onMouseLeave={() => setHoveredClose(false)}
            >
                <IconContext.Provider
                    value={{
                        color: 'black',
                        className: 'w-full h-full'
                    }}
                >
                    {hoveredClose ? <IoCloseCircle /> : <IoCloseCircleOutline />}
                </IconContext.Provider>
            </div>
        </div>
    )
}
