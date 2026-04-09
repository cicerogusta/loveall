// Sistema de Anuncios
// Web: banners internos via componente AdBanner.jsx
// Mobile nativo: AdMob carregado via plugin Capacitor no projeto Android/iOS
// O AdMob NAO e importado aqui - so funciona no runtime nativo via globalThis

const AD_CONFIG = {
  interstitialFrequency: 5,
  rewardedBonus: 'super_like',
};

let swipeCount = 0;

export async function initAds() {
  // No mobile nativo, o plugin AdMob e registrado via Capacitor
  // e acessado pelo bridge nativo - nao precisa de import JS aqui.
  // A integracao real acontece no codigo nativo (Android/iOS).
  console.log('Ad system initialized (web mode)');
}

// Interstitial - a cada N swipes, mostra ad interno (web) ou AdMob (nativo)
export async function showInterstitial() {
  swipeCount++;
  if (swipeCount % AD_CONFIG.interstitialFrequency !== 0) return false;
  // Retorna true para que o componente React mostre o ad web
  return true;
}

// Rewarded - usuario assiste voluntariamente para ganhar super like
export async function showRewarded() {
  return true; // Web sempre mostra o ad interno
}

export function getSwipeCount() { return swipeCount; }
export { AD_CONFIG };
