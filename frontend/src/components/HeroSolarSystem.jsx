import { useEffect, useRef } from 'react';
import './HeroSolarSystem.css';
import { services } from '../data/site.js';
import { useI18n } from '../i18n/I18nProvider.jsx';

const orbitalSizes = [230, 290, 350, 410, 290, 350, 410];
const orbitalPeriods = [14, 19, 25, 31, 22, 28, 17];
const orbitalAngles = [0, 51, 103, 154, 206, 257, 309];
const planetColors = ['#63d5ff', '#f5bf54', '#a895ff', '#5dd7b2', '#f18fb0', '#72aaff', '#c4dc75'];

export default function HeroSolarSystem() {
  const { t } = useI18n();
  const orbitRefs = useRef([]);
  const uprightRefs = useRef([]);
  const hoveredPlanets = useRef(new Set());
  const focusedPlanets = useRef(new Set());

  useEffect(() => {
    const initialAngles = [...orbitalAngles];
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      initialAngles.forEach((angle, index) => {
        if (orbitRefs.current[index]) {
          orbitRefs.current[index].style.transform = `translate(-50%, -50%) rotate(${angle}deg)`;
        }
        if (uprightRefs.current[index]) {
          uprightRefs.current[index].style.transform = `rotate(${-angle}deg)`;
        }
      });
      return undefined;
    }

    let frameId;
    let previousTime = performance.now();
    let speed = 1;
    const angles = [...orbitalAngles];

    function animate(now) {
      const delta = Math.min(now - previousTime, 64);
      previousTime = now;
      const isPaused = hoveredPlanets.current.size > 0 || focusedPlanets.current.size > 0;
      const targetSpeed = isPaused ? 0 : 1;
      const easing = 1 - Math.exp(-delta / 460);
      speed += (targetSpeed - speed) * easing;
      if (isPaused && speed < 0.001) speed = 0;

      angles.forEach((angle, index) => {
        const nextAngle = angle + (360 / orbitalPeriods[index]) * (delta / 1000) * speed;
        angles[index] = nextAngle;
        if (orbitRefs.current[index]) {
          orbitRefs.current[index].style.transform = `translate(-50%, -50%) rotate(${nextAngle}deg)`;
        }
        if (uprightRefs.current[index]) {
          uprightRefs.current[index].style.transform = `rotate(${-nextAngle}deg)`;
        }
      });

      frameId = requestAnimationFrame(animate);
    }

    frameId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(frameId);
  }, []);

  function trackInteraction(set, slug, active) {
    if (active) set.current.add(slug);
    else set.current.delete(slug);
  }

  return (
    <div className="hero-visual solar-system" role="group" aria-label={t('hero.artLabel')}>
      <span className="solar-ring solar-ring-near" aria-hidden="true" />
      <span className="solar-ring solar-ring-mid" aria-hidden="true" />
      <span className="solar-ring solar-ring-far" aria-hidden="true" />

      <div className="visual-core solar-core">
        <span className="core-symbol">C<span>.</span></span>
        <span className="core-caption">CERTECH SYSTEMS</span>
      </div>

      {services.map(({ slug }, index) => {
        const label = t(`services.${slug}.title`);

        return (
          <div
            className="solar-orbiting-planet"
            key={slug}
            ref={element => { orbitRefs.current[index] = element; }}
            style={{
              '--orbit-size': `${orbitalSizes[index]}px`,
              '--planet-color': planetColors[index],
            }}
          >
            <span
              className="solar-planet"
              tabIndex="0"
              role="img"
              aria-label={label}
              onPointerEnter={() => trackInteraction(hoveredPlanets, slug, true)}
              onPointerLeave={() => trackInteraction(hoveredPlanets, slug, false)}
              onFocus={() => trackInteraction(focusedPlanets, slug, true)}
              onBlur={() => trackInteraction(focusedPlanets, slug, false)}
            >
              <span
                className="solar-planet-upright"
                ref={element => { uprightRefs.current[index] = element; }}
              >
                <span className="solar-planet-face" aria-hidden="true" />
                <span className="solar-planet-label">{label}</span>
              </span>
            </span>
          </div>
        );
      })}
    </div>
  );
}
