package com.github.tvbox.osc.util;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.github.megatronking.stringfog.IStringFog;

import java.nio.charset.StandardCharsets;

/**
 * <pre>
 *     author : derek
 *     time   : 2022/12/01
 *     desc   :
 *     version:
 * </pre>
 */
public final class StringFogImpl2 implements IStringFog {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public byte[] encrypt(String data, byte[] key) {
        return xor(data.getBytes(StandardCharsets.UTF_8), key);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public String decrypt(byte[] data, byte[] key) {
        return new String(xor(data, key), StandardCharsets.UTF_8);
    }

    @Override
    public boolean shouldFog(String data) {
        return true;
    }

    private static byte[] xor(byte[] data, byte[] key) {
        int len = data.length;
        int lenKey = key.length;
        int i = 0;
        int j = 0;
        while (i < len) {
            if (j >= lenKey) {
                j = 0;
            }
            data[i] = (byte) (data[i] ^ key[j]);
            i++;
            j++;
        }
        return data;
    }

}
