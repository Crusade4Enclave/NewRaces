// • ▌ ▄ ·.  ▄▄▄·  ▄▄ • ▪   ▄▄· ▄▄▄▄·  ▄▄▄·  ▐▄▄▄  ▄▄▄ .
// ·██ ▐███▪▐█ ▀█ ▐█ ▀ ▪██ ▐█ ▌▪▐█ ▀█▪▐█ ▀█ •█▌ ▐█▐▌·
// ▐█ ▌▐▌▐█·▄█▀▀█ ▄█ ▀█▄▐█·██ ▄▄▐█▀▀█▄▄█▀▀█ ▐█▐ ▐▌▐▀▀▀
// ██ ██▌▐█▌▐█ ▪▐▌▐█▄▪▐█▐█▌▐███▌██▄▪▐█▐█ ▪▐▌██▐ █▌▐█▄▄▌
// ▀▀  █▪▀▀▀ ▀  ▀ ·▀▀▀▀ ▀▀▀·▀▀▀ ·▀▀▀▀  ▀  ▀ ▀▀  █▪ ▀▀▀
//      Magicbane Emulator Project © 2013 - 2022
//                www.magicbane.com

package engine.math;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicFloat {

    private final AtomicInteger fl;

    public AtomicFloat() {
        fl = new AtomicInteger(Float.floatToIntBits(0f));
    }

    public AtomicFloat(float value) {
        fl = new AtomicInteger(Float.floatToIntBits(value));
    }

    public float addAndGet(float delta) {
        int oldValue = fl.get();
        while (!fl.compareAndSet(oldValue, Float.floatToIntBits(Float.intBitsToFloat(oldValue) + delta))) {
            oldValue = fl.get();
        }
        return fl.get();
    }

    public boolean compareAndSet(float oldVal, float newVal) {
        return fl.compareAndSet(Float.floatToIntBits(oldVal), Float.floatToIntBits(newVal));
    }

    public float decrementAndGet() {
        int oldValue = fl.get();
        while (!fl.compareAndSet(oldValue, Float.floatToIntBits(Float.intBitsToFloat(oldValue) - 1f))) {
            oldValue = fl.get();
        }
        return fl.get();
    }

    public float get() {
        return Float.intBitsToFloat(fl.get());
    }

    public float getAndAdd(float delta) {
        int oldValue = fl.get();
        while (!fl.compareAndSet(oldValue, Float.floatToIntBits(Float.intBitsToFloat(oldValue) + delta))) {
            oldValue = fl.get();
        }
        return oldValue;
    }

    public float getAndIncrement() {
        int oldValue = fl.get();
        while (!fl.compareAndSet(oldValue, Float.floatToIntBits(Float.intBitsToFloat(oldValue) + 1f))) {
            oldValue = fl.get();
        }
        return oldValue;
    }

    public float getAndDecrement(float delta) {
        int oldValue = fl.get();
        while (!fl.compareAndSet(oldValue, Float.floatToIntBits(Float.intBitsToFloat(oldValue) - 1f))) {
            oldValue = fl.get();
        }
        return oldValue;
    }

    public float getAndSet(float value) {
        return Float.intBitsToFloat(fl.getAndSet(Float.floatToIntBits(value)));
    }

    public float incrementAndGet() {
        int oldValue = fl.get();
        while (!fl.compareAndSet(oldValue, Float.floatToIntBits(Float.intBitsToFloat(oldValue) + 1f))) {
            oldValue = fl.get();
        }
        return fl.get();
    }

    public void lazySet(float value) {
        fl.lazySet(Float.floatToIntBits(value));
    }

    public void set(float value) {
        fl.set(Float.floatToIntBits(value));
    }

    @Override
    public String toString() {
        return fl.toString();
    }

    public boolean weakCompareAndSet(float oldVal, float newVal) {
        return fl.weakCompareAndSet(Float.floatToIntBits(oldVal), Float.floatToIntBits(newVal));
    }
}
