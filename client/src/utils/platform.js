import { Capacitor } from '@capacitor/core';

const _isNative = Capacitor.isNativePlatform();
const _platform = Capacitor.getPlatform(); // 'web' | 'android' | 'ios'

export async function initPlatform() {
  if (_isNative) {
    try {
      const { StatusBar } = await import('@capacitor/status-bar');
      await StatusBar.setBackgroundColor({ color: '#0f0f14' });
      await StatusBar.setStyle({ style: 'DARK' });
    } catch {}

  }
  return { isNative: _isNative, platform: _platform };
}

export function isNative() { return _isNative; }
export function getPlatform() { return _platform; }

export async function hapticFeedback(type = 'light') {
  if (!_isNative) return;
  try {
    const { Haptics, ImpactStyle } = await import('@capacitor/haptics');
    const style = type === 'heavy' ? ImpactStyle.Heavy : type === 'medium' ? ImpactStyle.Medium : ImpactStyle.Light;
    await Haptics.impact({ style });
  } catch {}
}
