import React, { useEffect, useState, MouseEvent, PropsWithChildren, useRef } from 'react'

import { ChromePicker } from 'react-color'
import { AppContextProvider, useAppContext } from './app-context'
import { GameRenderer } from './playback/GameRenderer'
import { NumInput } from './components/forms'
import {
    Colors,
    currentColors,
    updateGlobalColor,
    getGlobalColor,
    resetGlobalColors,
    DEFAULT_GLOBAL_COLORS
} from './colors'
import { BrightButton, Button } from './components/button'

export type ClientConfig = typeof DEFAULT_CONFIG

interface Props {
    open: boolean
}

const DEFAULT_CONFIG = {
    showAllIndicators: false,
    showAllRobotRadii: false,
    showTimelineMarkers: true,
    showHealthBars: true,
    showPaintBars: false,
    showPaintMarkers: true,
    showMapXY: true,
    enableFancyPaint: true,
    streamRunnerGames: true,
    profileGames: false,
    validateMaps: false,
    resolutionScale: 100,
    colors: {
        [Colors.TEAM_ONE]: '#cdcdcc',
        [Colors.TEAM_TWO]: '#fee493',

        [Colors.PAINT_TEAMONE_ONE]: '#666666',
        [Colors.PAINT_TEAMONE_TWO]: '#565656',
        [Colors.PAINT_TEAMTWO_ONE]: '#b28b52',
        [Colors.PAINT_TEAMTWO_TWO]: '#997746',
        [Colors.WALLS_COLOR]: '#547f31',
        [Colors.TILE_COLOR]: '#4c301e',
        [Colors.GAMEAREA_BACKGROUND]: '#2e2323',
        [Colors.SIDEBAR_BACKGROUND]: '#3f3131'
    } as Record<Colors, string>
}

const configDescription: Record<keyof ClientConfig, string> = {
    showAllIndicators: 'Show all indicator dots and lines',
    showAllRobotRadii: 'Show all robot view and attack radii',
    showTimelineMarkers: 'Show user-generated markers on the timeline',
    showHealthBars: 'Show health bars below all robots',
    showPaintBars: 'Show paint bars below all robots',
    showPaintMarkers: 'Show paint markers created using mark()',
    showMapXY: 'Show X,Y when hovering a tile',
    enableFancyPaint: 'Enable fancy paint rendering',
    streamRunnerGames: 'Stream each round from the runner live as the game is being played',
    profileGames: 'Enable saving profiling data when running games',
    validateMaps: 'Validate maps before running a game',
    resolutionScale: 'Resolution scale for the game area. Decrease to help performance.',
    colors: ''
}

export function getDefaultConfig(): ClientConfig {
    const config: ClientConfig = { ...DEFAULT_CONFIG }
    for (const key in config) {
        const value = localStorage.getItem('config' + key)
        if (value) {
            ;(config[key as keyof ClientConfig] as any) = JSON.parse(value)
        }
    }

    for (const key in config.colors) {
        const value = localStorage.getItem('config-colors' + key)
        if (value) {
            config.colors[key as Colors] = JSON.parse(value)
            updateGlobalColor(key as Colors, JSON.parse(value))
        }
    }

    return config
}

//const ColorRow = (props: { displayName: string, colorName: Colors }) => {
//    return (
//        <>
//            <ColorPicker name={colorName} />
//        </>
//    )
//}

const ColorPicker = (props: { displayName: string; colorName: Colors }) => {
    const context = useAppContext()
    const value = context.state.config.colors[props.colorName]
    //const wrapperRef = React.createRef()
    const ref = useRef<HTMLDivElement>(null)
    const buttonRef = useRef<HTMLButtonElement>(null)
    //this.handleClickOutside = this.handleClickOutside.bind(this);

    const [displayColorPicker, setDisplayColorPicker] = useState(false)

    const handleClick = () => {
        setDisplayColorPicker(!displayColorPicker)
    }

    const handleClose = () => {
        setDisplayColorPicker(false)
    }

    //const handleClickOutside = (event) => {
    //    if (wrapperRef && !wrapperRef.contains(event.target)) {
    //        alert('You clicked outside of me!')
    //    }
    //}

    const handleClickOutside = (event: any) => {
        if (
            ref.current &&
            buttonRef.current &&
            !ref.current.contains(event.target) &&
            !buttonRef.current.contains(event.target)
        ) {
            //alert('hi')
            handleClose()
        }
    }

    addEventListener('mousedown', handleClickOutside)

    const onChange = (newColor: any) => {
        updateGlobalColor(props.colorName, newColor.hex)
        context.setState((prevState) => ({
            ...prevState,
            config: { ...prevState.config, colors: { ...prevState.config.colors, [props.colorName]: newColor.hex } }
        }))
        // hopefully after the setState is done
        setTimeout(() => GameRenderer.render(), 10)
    }

    return (
        <>
            <div className={'ml-2 mb-2 text-xs flex flex-start justify-start items-center'}>
                {/*Background:*/}
                {props.displayName}:
                <button
                    ref={buttonRef}
                    className={'text-xs ml-2 px-4 py-3 flex flex-row hover:bg-cyanDark rounded-md text-white'}
                    style={{ backgroundColor: value, border: '2px solid white' }}
                    onClick={handleClick}
                ></button>
            </div>
            <div ref={ref}>{displayColorPicker && <ChromePicker color={value} onChange={onChange} />}</div>
        </>
    )
}

export const ConfigPage: React.FC<Props> = (props) => {
    if (!props.open) return null
    const context = useAppContext()

    return (
        <div className={'flex flex-col'}>
            <div className="mb-2">Edit Client Config:</div>
            {Object.entries(DEFAULT_CONFIG).map(([k, v]) => {
                const key = k as keyof ClientConfig
                if (typeof v === 'string') return <ConfigStringElement configKey={key} key={key} />
                if (typeof v === 'boolean') return <ConfigBooleanElement configKey={key} key={key} />
                if (typeof v === 'number') return <ConfigNumberElement configKey={key} key={key} />
            })}

            <div>
                <br></br>
            </div>
            <div className="color-pickers">
                {/*fake class*/}
                Customize Colors:
                <div className="text-sm pb-1 pt-1">Interface</div>
                <ColorPicker displayName={'Background'} colorName={Colors.GAMEAREA_BACKGROUND} />
                <ColorPicker displayName={'Sidebar'} colorName={Colors.SIDEBAR_BACKGROUND} />
                <div className="text-sm pb-1">General</div>
                <ColorPicker displayName={'Walls'} colorName={Colors.WALLS_COLOR} />
                <ColorPicker displayName={'Tiles'} colorName={Colors.TILE_COLOR} />
                <div className="text-sm pb-1">Silver</div>
                <ColorPicker displayName={'Text'} colorName={Colors.TEAM_ONE} />
                <ColorPicker displayName={'Primary Paint'} colorName={Colors.PAINT_TEAMONE_ONE} />
                <ColorPicker displayName={'Secondary Paint'} colorName={Colors.PAINT_TEAMONE_TWO} />
                <div className="text-sm pb-1">Gold</div>
                <ColorPicker displayName={'Text'} colorName={Colors.TEAM_TWO} />
                <ColorPicker displayName={'Primary Paint'} colorName={Colors.PAINT_TEAMTWO_ONE} />
                <ColorPicker displayName={'Secondary Paint'} colorName={Colors.PAINT_TEAMTWO_TWO} />
            </div>
            <div className="flex flex-row mt-8">
                <BrightButton
                    className=""
                    onClick={() => {
                        resetGlobalColors()

                        context.setState((prevState) => ({
                            ...prevState,
                            config: { ...prevState.config, colors: { ...DEFAULT_GLOBAL_COLORS } }
                        }))
                    }}
                >
                    Reset Colors
                </BrightButton>
            </div>
        </div>
    )
}

const ConfigBooleanElement: React.FC<{ configKey: keyof ClientConfig }> = ({ configKey }) => {
    const context = useAppContext()
    const value = context.state.config[configKey] as boolean
    return (
        <div className={'flex flex-row items-center mb-2'}>
            <input
                type={'checkbox'}
                checked={value as any}
                onChange={(e) => {
                    context.setState((prevState) => ({
                        ...prevState,
                        config: { ...prevState.config, [configKey]: e.target.checked }
                    }))
                    localStorage.setItem('config' + configKey, JSON.stringify(e.target.checked))
                    // hopefully after the setState is done
                    setTimeout(() => GameRenderer.fullRender(), 10)
                }}
            />
            <div className={'ml-2 text-xs'}>{configDescription[configKey] ?? configKey}</div>
        </div>
    )
}

const ConfigStringElement: React.FC<{ configKey: string }> = ({ configKey }) => {
    const context = useAppContext()
    const value = context.state.config[configKey as keyof ClientConfig]
    return <div className={'flex flex-row items-center'}>Todo</div>
}

const ConfigNumberElement: React.FC<{ configKey: keyof ClientConfig }> = ({ configKey }) => {
    const context = useAppContext()
    const value = context.state.config[configKey as keyof ClientConfig] as number
    return (
        <div className={'flex flex-row items-center mb-2'}>
            <NumInput
                value={value}
                changeValue={(newVal) => {
                    context.setState((prevState) => ({
                        ...prevState,
                        config: { ...context.state.config, [configKey]: newVal }
                    }))
                    localStorage.setItem('config' + configKey, JSON.stringify(newVal))
                    // hopefully after the setState is done
                    setTimeout(() => {
                        // Trigger canvas dimension update to ensure resolution is updated
                        GameRenderer.onMatchChange()
                    }, 10)
                }}
                min={10}
                max={200}
            />
            <div className={'ml-2 text-xs'}>{configDescription[configKey] ?? configKey}</div>
        </div>
    )
}
