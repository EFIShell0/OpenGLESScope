#include <jni.h>
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl32.h>
#include <algorithm>
#include <cctype>
#include <cstdint>
#include <cmath>
#include <limits>
#include <iomanip>
#include <sstream>
#include <string>
#include <vector>
#include <utility>

static std::string esc(const std::string& s) {
    std::ostringstream o;
    const auto unicodeEscape = [&](uint32_t cp) {
        o << "\\u" << std::uppercase << std::hex << std::setw(4) << std::setfill('0') << cp << std::dec;
    };
    for (size_t i = 0; i < s.size();) {
        const auto c = static_cast<unsigned char>(s[i]);
        if (c < 0x80) {
            ++i;
            switch (c) {
                case '"': o << "\\\""; break;
                case '\\': o << "\\\\"; break;
                case '\b': o << "\\b"; break;
                case '\f': o << "\\f"; break;
                case '\n': o << "\\n"; break;
                case '\r': o << "\\r"; break;
                case '\t': o << "\\t"; break;
                default: if (c < 0x20) unicodeEscape(c); else o << static_cast<char>(c); break;
            }
            continue;
        }
        const size_t length = c >= 0xC2 && c <= 0xDF ? 2 :
                              c >= 0xE0 && c <= 0xEF ? 3 :
                              c >= 0xF0 && c <= 0xF4 ? 4 : 0;
        bool valid = length != 0 && i + length <= s.size();
        uint32_t cp = length == 2 ? c & 0x1F : length == 3 ? c & 0x0F : c & 0x07;
        if (valid) {
            for (size_t j = 1; j < length; ++j) {
                const auto continuation = static_cast<unsigned char>(s[i + j]);
                if ((continuation & 0xC0) != 0x80) { valid = false; break; }
                cp = (cp << 6) | (continuation & 0x3F);
            }
            valid = valid && cp >= (length == 2 ? 0x80u : length == 3 ? 0x800u : 0x10000u)
                    && cp <= 0x10FFFFu && !(cp >= 0xD800 && cp <= 0xDFFF);
        }
        if (!valid) {
            unicodeEscape(0xFFFD);
            ++i;
            continue;
        }
        if (cp <= 0xFFFFu) unicodeEscape(cp);
        else {
            cp -= 0x10000u;
            unicodeEscape(0xD800u + (cp >> 10));
            unicodeEscape(0xDC00u + (cp & 0x3FFu));
        }
        i += length;
    }
    return o.str();
}

static std::string q(const char* s) { return std::string("\"") + esc(s ? s : "") + "\""; }
static std::string q(const std::string& s) { return std::string("\"") + esc(s) + "\""; }
static std::string hexv(EGLint v) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<unsigned int>(v); return o.str(); }
static std::string hexModifier(EGLuint64KHR value) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << std::setw(16) << std::setfill('0') << static_cast<uint64_t>(value); return o.str(); }
static bool stableCount(EGLint expected, EGLint written) { return expected >= 0 && written == expected; }

static constexpr GLint kMaxGlEnumerationCount = 16384;
static constexpr EGLint kMaxEglConfigCount = 4096;
static constexpr GLint kMaxProgramBinaryBytes = 8 * 1024 * 1024;
static constexpr size_t kMaxRuntimeStringBytes = 1024 * 1024;
static constexpr size_t kMaxExtensionTokenBytes = 4096;
static constexpr GLint kMaxInfoLogBytes = 1024 * 1024;
static constexpr GLint kMaxInternalFormatSampleCounts = 64;
static constexpr EGLint EGL_RECORDABLE_ANDROID_VALUE = 0x3142;
static constexpr EGLint EGL_FRAMEBUFFER_TARGET_ANDROID_VALUE = 0x3147;
static constexpr EGLint EGL_COLOR_COMPONENT_TYPE_EXT_VALUE = 0x3339;
static constexpr EGLint EGL_COLOR_COMPONENT_TYPE_FIXED_EXT_VALUE = 0x333A;
static constexpr EGLint EGL_COLOR_COMPONENT_TYPE_FLOAT_EXT_VALUE = 0x333B;
static constexpr EGLint EGL_DEVICE_EXT_VALUE = 0x322C;
static constexpr EGLint EGL_RENDERER_EXT_VALUE = 0x335F;
static constexpr EGLint EGL_DRIVER_NAME_EXT_VALUE = 0x335E;
static constexpr EGLint EGL_DEVICE_TYPE_EXT_VALUE = 0x3590;
static constexpr EGLint EGL_DEVICE_TYPE_OTHER_EXT_VALUE = 0x3591;
static constexpr EGLint EGL_DEVICE_TYPE_INTEGRATED_GPU_EXT_VALUE = 0x3592;
static constexpr EGLint EGL_DEVICE_TYPE_DISCRETE_GPU_EXT_VALUE = 0x3593;
static constexpr EGLint EGL_DEVICE_TYPE_CPU_EXT_VALUE = 0x3594;
static constexpr EGLint EGL_PROTECTED_CONTENT_EXT_VALUE = 0x32C0;
static constexpr EGLint EGL_TRACK_REFERENCES_KHR_VALUE = 0x3352;
static constexpr GLint GL_MOTION_ESTIMATION_SEARCH_BLOCK_X_QCOM_VALUE = 0x8C90;
static constexpr GLint GL_MOTION_ESTIMATION_SEARCH_BLOCK_Y_QCOM_VALUE = 0x8C91;
static constexpr GLint GL_SHADING_RATE_IMAGE_TEXEL_WIDTH_NV_VALUE = 0x955C;
static constexpr GLint GL_SHADING_RATE_IMAGE_TEXEL_HEIGHT_NV_VALUE = 0x955D;
static constexpr GLint GL_SHADING_RATE_IMAGE_PALETTE_SIZE_NV_VALUE = 0x955E;
static constexpr GLint GL_MAX_COARSE_FRAGMENT_SAMPLES_NV_VALUE = 0x955F;
static constexpr GLint GL_SHADING_RATE_IMAGE_PALETTE_COUNT_NV_VALUE = 0x95B2;
static constexpr GLint GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM_VALUE = 0x8F66;
static constexpr EGLint kMaxEglDeviceCount = 32;
static constexpr EGLint kMaxEglCompressionRateCount = 32;
static constexpr size_t kMaxEglCapabilityCount = 256;
static constexpr EGLint kMaxDmaBufFormatCount = 128;
static constexpr EGLint kMaxDmaBufModifierCountPerFormat = 256;
static constexpr size_t kMaxDmaBufModifierCountTotal = 4096;

static std::string eglErrorName(EGLint error) {
    switch (error) {
        case EGL_SUCCESS: return "EGL_SUCCESS";
        case EGL_NOT_INITIALIZED: return "EGL_NOT_INITIALIZED";
        case EGL_BAD_ACCESS: return "EGL_BAD_ACCESS";
        case EGL_BAD_ALLOC: return "EGL_BAD_ALLOC";
        case EGL_BAD_ATTRIBUTE: return "EGL_BAD_ATTRIBUTE";
        case EGL_BAD_CONTEXT: return "EGL_BAD_CONTEXT";
        case EGL_BAD_CONFIG: return "EGL_BAD_CONFIG";
        case EGL_BAD_CURRENT_SURFACE: return "EGL_BAD_CURRENT_SURFACE";
        case EGL_BAD_DISPLAY: return "EGL_BAD_DISPLAY";
        case EGL_BAD_MATCH: return "EGL_BAD_MATCH";
        case EGL_BAD_NATIVE_PIXMAP: return "EGL_BAD_NATIVE_PIXMAP";
        case EGL_BAD_NATIVE_WINDOW: return "EGL_BAD_NATIVE_WINDOW";
        case EGL_BAD_PARAMETER: return "EGL_BAD_PARAMETER";
        case EGL_BAD_SURFACE: return "EGL_BAD_SURFACE";
#ifdef EGL_CONTEXT_LOST
        case EGL_CONTEXT_LOST: return "EGL_CONTEXT_LOST";
#endif
        default: return "Unknown EGL error";
    }
}

static std::string eglErrorDisplay(EGLint error) { return eglErrorName(error) + " (" + hexv(error) + ")"; }

static void releaseEgl(EGLDisplay d, EGLSurface surface, EGLContext context) {
    if (d != EGL_NO_DISPLAY) {
        eglMakeCurrent(d, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        if (surface != EGL_NO_SURFACE) eglDestroySurface(d, surface);
        if (context != EGL_NO_CONTEXT) eglDestroyContext(d, context);
        eglTerminate(d);
    }
    eglReleaseThread();
}

struct EglAttrResult { EGLint value; bool available; EGLint error; };
struct EglRuntimeQuery { const char* name; EglAttrResult result; bool applicable; const char* notApplicableDetail; };
struct EglConfigQuery { const char* key; EglAttrResult result; bool applicable; const char* notApplicableDetail; };

static EglAttrResult queryConfigAttr(EGLDisplay d, EGLConfig c, EGLint attr) {
    eglGetError();
    EGLint value = 0;
    if (eglGetConfigAttrib(d, c, attr, &value) == EGL_TRUE) return {value, true, EGL_SUCCESS};
    return {0, false, eglGetError()};
}

static EglAttrResult queryContextAttr(EGLDisplay d, EGLContext c, EGLint attr) {
    eglGetError();
    EGLint value = 0;
    if (eglQueryContext(d, c, attr, &value) == EGL_TRUE) return {value, true, EGL_SUCCESS};
    return {0, false, eglGetError()};
}

static EglAttrResult querySurfaceAttr(EGLDisplay d, EGLSurface surface, EGLint attr) {
    eglGetError();
    EGLint value = 0;
    if (eglQuerySurface(d, surface, attr, &value) == EGL_TRUE) return {value, true, EGL_SUCCESS};
    return {0, false, eglGetError()};
}

static std::string eglApiDisplay(EGLenum api) {
    if (api == EGL_OPENGL_ES_API) return "EGL_OPENGL_ES_API (0x30A0)";
    if (api == EGL_OPENGL_API) return "EGL_OPENGL_API (0x30A2)";
    if (api == EGL_OPENVG_API) return "EGL_OPENVG_API (0x30A1)";
    return hexv(static_cast<EGLint>(api));
}

static std::string eglEnumDisplay(EGLint value) {
    if (value == EGL_BACK_BUFFER) return "EGL_BACK_BUFFER (0x3084)";
    if (value == EGL_SINGLE_BUFFER) return "EGL_SINGLE_BUFFER (0x3085)";
    if (value == EGL_BUFFER_PRESERVED) return "EGL_BUFFER_PRESERVED (0x3094)";
    if (value == EGL_BUFFER_DESTROYED) return "EGL_BUFFER_DESTROYED (0x3095)";
    if (value == EGL_NO_TEXTURE) return "EGL_NO_TEXTURE (0x305C)";
    if (value == EGL_TEXTURE_RGB) return "EGL_TEXTURE_RGB (0x305D)";
    if (value == EGL_TEXTURE_RGBA) return "EGL_TEXTURE_RGBA (0x305E)";
    if (value == EGL_TEXTURE_2D) return "EGL_TEXTURE_2D (0x305F)";
    if (value == EGL_MULTISAMPLE_RESOLVE_DEFAULT) return "EGL_MULTISAMPLE_RESOLVE_DEFAULT (0x309A)";
    if (value == EGL_MULTISAMPLE_RESOLVE_BOX) return "EGL_MULTISAMPLE_RESOLVE_BOX (0x309B)";
    if (value == EGL_OPENGL_ES_API) return "EGL_OPENGL_ES_API (0x30A0)";
    if (value == EGL_OPENGL_API) return "EGL_OPENGL_API (0x30A2)";
    if (value == EGL_OPENVG_API) return "EGL_OPENVG_API (0x30A1)";
    if (value == EGL_COLOR_COMPONENT_TYPE_FIXED_EXT_VALUE) return "EGL_COLOR_COMPONENT_TYPE_FIXED_EXT (0x333A)";
    if (value == EGL_COLOR_COMPONENT_TYPE_FLOAT_EXT_VALUE) return "EGL_COLOR_COMPONENT_TYPE_FLOAT_EXT (0x333B)";
    return hexv(value);
}

static std::string eglGlColorspaceDisplay(EGLint value) {
    if (value == EGL_GL_COLORSPACE_SRGB) return "EGL_GL_COLORSPACE_SRGB (0x3089)";
    if (value == EGL_GL_COLORSPACE_LINEAR) return "EGL_GL_COLORSPACE_LINEAR (0x308A)";
    if (value == 0x314D) return "EGL_GL_COLORSPACE_DEFAULT_EXT (0x314D)";
    if (value == 0x333F) return "EGL_GL_COLORSPACE_BT2020_LINEAR_EXT (0x333F)";
    if (value == 0x3340) return "EGL_GL_COLORSPACE_BT2020_PQ_EXT (0x3340)";
    if (value == 0x3350) return "EGL_GL_COLORSPACE_SCRGB_LINEAR_EXT (0x3350)";
    if (value == 0x3351) return "EGL_GL_COLORSPACE_SCRGB_EXT (0x3351)";
    if (value == 0x3362) return "EGL_GL_COLORSPACE_DISPLAY_P3_LINEAR_EXT (0x3362)";
    if (value == 0x3363) return "EGL_GL_COLORSPACE_DISPLAY_P3_EXT (0x3363)";
    if (value == 0x3490) return "EGL_GL_COLORSPACE_DISPLAY_P3_PASSTHROUGH_EXT (0x3490)";
    if (value == 0x3540) return "EGL_GL_COLORSPACE_BT2020_HLG_EXT (0x3540)";
    return hexv(value);
}

static std::string eglVgColorspaceDisplay(EGLint value) {
    if (value == EGL_VG_COLORSPACE_sRGB) return "EGL_VG_COLORSPACE_sRGB (0x3089)";
    if (value == EGL_VG_COLORSPACE_LINEAR) return "EGL_VG_COLORSPACE_LINEAR (0x308A)";
    return hexv(value);
}

static std::string eglVgAlphaFormatDisplay(EGLint value) {
    if (value == EGL_VG_ALPHA_FORMAT_NONPRE) return "EGL_VG_ALPHA_FORMAT_NONPRE (0x308B)";
    if (value == EGL_VG_ALPHA_FORMAT_PRE) return "EGL_VG_ALPHA_FORMAT_PRE (0x308C)";
    return hexv(value);
}

static std::string eglDeviceTypeDisplay(EGLAttrib value) {
    if (value == EGL_DEVICE_TYPE_OTHER_EXT_VALUE) return "EGL_DEVICE_TYPE_OTHER_EXT (0x3591)";
    if (value == EGL_DEVICE_TYPE_INTEGRATED_GPU_EXT_VALUE) return "EGL_DEVICE_TYPE_INTEGRATED_GPU_EXT (0x3592)";
    if (value == EGL_DEVICE_TYPE_DISCRETE_GPU_EXT_VALUE) return "EGL_DEVICE_TYPE_DISCRETE_GPU_EXT (0x3593)";
    if (value == EGL_DEVICE_TYPE_CPU_EXT_VALUE) return "EGL_DEVICE_TYPE_CPU_EXT (0x3594)";
    std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<uintptr_t>(value); return o.str();
}

static std::string eglCompressionRateDisplay(EGLint value) {
    if (value >= 0x34B4 && value <= 0x34BF) {
        static constexpr const char* kFixedRateNames[] = {
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_1BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_2BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_3BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_4BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_5BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_6BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_7BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_8BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_9BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_10BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_11BPC_EXT",
            "EGL_SURFACE_COMPRESSION_FIXED_RATE_12BPC_EXT",
        };
        return std::string(kFixedRateNames[value - 0x34B4]) + " (" + hexv(value) + ")";
    }
    return hexv(value);
}

static std::string enumDisplay(GLint value, const std::string& category) {
    if (category == "compressedFormats") {
        switch (static_cast<GLenum>(value)) {
            case GL_COMPRESSED_R11_EAC: return "GL_COMPRESSED_R11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_SIGNED_R11_EAC: return "GL_COMPRESSED_SIGNED_R11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_RG11_EAC: return "GL_COMPRESSED_RG11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_SIGNED_RG11_EAC: return "GL_COMPRESSED_SIGNED_RG11_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_RGB8_ETC2: return "GL_COMPRESSED_RGB8_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_SRGB8_ETC2: return "GL_COMPRESSED_SRGB8_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2: return "GL_COMPRESSED_RGB8_PUNCHTHROUGH_ALPHA1_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2: return "GL_COMPRESSED_SRGB8_PUNCHTHROUGH_ALPHA1_ETC2 (" + hexv(value) + ")";
            case GL_COMPRESSED_RGBA8_ETC2_EAC: return "GL_COMPRESSED_RGBA8_ETC2_EAC (" + hexv(value) + ")";
            case GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC: return "GL_COMPRESSED_SRGB8_ALPHA8_ETC2_EAC (" + hexv(value) + ")";
            default: break;
        }
        if (value == 0x87F9) return "GL_3DC_X_AMD (" + hexv(value) + ")";
        if (value == 0x87FA) return "GL_3DC_XY_AMD (" + hexv(value) + ")";
        if (value == 0x8C92) return "GL_ATC_RGB_AMD (" + hexv(value) + ")";
        if (value == 0x8C93) return "GL_ATC_RGBA_EXPLICIT_ALPHA_AMD (" + hexv(value) + ")";
        if (value == 0x87EE) return "GL_ATC_RGBA_INTERPOLATED_ALPHA_AMD (" + hexv(value) + ")";
        if (value == 0x8D64) return "GL_ETC1_RGB8_OES (" + hexv(value) + ")";
        if (value == 0x8B90) return "GL_PALETTE4_RGB8_OES (" + hexv(value) + ")";
        if (value == 0x8B91) return "GL_PALETTE4_RGBA8_OES (" + hexv(value) + ")";
        if (value == 0x8B92) return "GL_PALETTE4_R5_G6_B5_OES (" + hexv(value) + ")";
        if (value == 0x8B93) return "GL_PALETTE4_RGBA4_OES (" + hexv(value) + ")";
        if (value == 0x8B94) return "GL_PALETTE4_RGB5_A1_OES (" + hexv(value) + ")";
        if (value == 0x8B95) return "GL_PALETTE8_RGB8_OES (" + hexv(value) + ")";
        if (value == 0x8B96) return "GL_PALETTE8_RGBA8_OES (" + hexv(value) + ")";
        if (value == 0x8B97) return "GL_PALETTE8_R5_G6_B5_OES (" + hexv(value) + ")";
        if (value == 0x8B98) return "GL_PALETTE8_RGBA4_OES (" + hexv(value) + ")";
        if (value == 0x8B99) return "GL_PALETTE8_RGB5_A1_OES (" + hexv(value) + ")";
        if (value == 0x83F0) return "GL_COMPRESSED_RGB_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x83F1) return "GL_COMPRESSED_RGBA_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x83F2) return "GL_COMPRESSED_RGBA_S3TC_DXT3_EXT (" + hexv(value) + ")";
        if (value == 0x83F3) return "GL_COMPRESSED_RGBA_S3TC_DXT5_EXT (" + hexv(value) + ")";
        if (value == 0x8C4C) return "GL_COMPRESSED_SRGB_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x8C4D) return "GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT1_EXT (" + hexv(value) + ")";
        if (value == 0x8C4E) return "GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT3_EXT (" + hexv(value) + ")";
        if (value == 0x8C4F) return "GL_COMPRESSED_SRGB_ALPHA_S3TC_DXT5_EXT (" + hexv(value) + ")";
        if (value == 0x8DBB) return "GL_COMPRESSED_RED_RGTC1_EXT (" + hexv(value) + ")";
        if (value == 0x8DBC) return "GL_COMPRESSED_SIGNED_RED_RGTC1_EXT (" + hexv(value) + ")";
        if (value == 0x8DBD) return "GL_COMPRESSED_RED_GREEN_RGTC2_EXT (" + hexv(value) + ")";
        if (value == 0x8DBE) return "GL_COMPRESSED_SIGNED_RED_GREEN_RGTC2_EXT (" + hexv(value) + ")";
        if (value == 0x8E8C) return "GL_COMPRESSED_RGBA_BPTC_UNORM_EXT (" + hexv(value) + ")";
        if (value == 0x8E8D) return "GL_COMPRESSED_SRGB_ALPHA_BPTC_UNORM_EXT (" + hexv(value) + ")";
        if (value == 0x8E8E) return "GL_COMPRESSED_RGB_BPTC_SIGNED_FLOAT_EXT (" + hexv(value) + ")";
        if (value == 0x8E8F) return "GL_COMPRESSED_RGB_BPTC_UNSIGNED_FLOAT_EXT (" + hexv(value) + ")";
        if (value == 0x8C00) return "GL_COMPRESSED_RGB_PVRTC_4BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C01) return "GL_COMPRESSED_RGB_PVRTC_2BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C02) return "GL_COMPRESSED_RGBA_PVRTC_4BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C03) return "GL_COMPRESSED_RGBA_PVRTC_2BPPV1_IMG (" + hexv(value) + ")";
        if (value == 0x8C70) return "GL_COMPRESSED_LUMINANCE_LATC1_EXT (" + hexv(value) + ")";
        if (value == 0x8C71) return "GL_COMPRESSED_SIGNED_LUMINANCE_LATC1_EXT (" + hexv(value) + ")";
        if (value == 0x8C72) return "GL_COMPRESSED_LUMINANCE_ALPHA_LATC2_EXT (" + hexv(value) + ")";
        if (value == 0x8C73) return "GL_COMPRESSED_SIGNED_LUMINANCE_ALPHA_LATC2_EXT (" + hexv(value) + ")";
        if (value == 0x9137) return "GL_COMPRESSED_RGBA_PVRTC_2BPPV2_IMG (" + hexv(value) + ")";
        if (value == 0x9138) return "GL_COMPRESSED_RGBA_PVRTC_4BPPV2_IMG (" + hexv(value) + ")";
        if (value == 0x93B0) return "GL_COMPRESSED_RGBA_ASTC_4x4_KHR (" + hexv(value) + ")";
        if (value == 0x93B1) return "GL_COMPRESSED_RGBA_ASTC_5x4_KHR (" + hexv(value) + ")";
        if (value == 0x93B2) return "GL_COMPRESSED_RGBA_ASTC_5x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B3) return "GL_COMPRESSED_RGBA_ASTC_6x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B4) return "GL_COMPRESSED_RGBA_ASTC_6x6_KHR (" + hexv(value) + ")";
        if (value == 0x93B5) return "GL_COMPRESSED_RGBA_ASTC_8x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B6) return "GL_COMPRESSED_RGBA_ASTC_8x6_KHR (" + hexv(value) + ")";
        if (value == 0x93B7) return "GL_COMPRESSED_RGBA_ASTC_8x8_KHR (" + hexv(value) + ")";
        if (value == 0x93B8) return "GL_COMPRESSED_RGBA_ASTC_10x5_KHR (" + hexv(value) + ")";
        if (value == 0x93B9) return "GL_COMPRESSED_RGBA_ASTC_10x6_KHR (" + hexv(value) + ")";
        if (value == 0x93BA) return "GL_COMPRESSED_RGBA_ASTC_10x8_KHR (" + hexv(value) + ")";
        if (value == 0x93BB) return "GL_COMPRESSED_RGBA_ASTC_10x10_KHR (" + hexv(value) + ")";
        if (value == 0x93BC) return "GL_COMPRESSED_RGBA_ASTC_12x10_KHR (" + hexv(value) + ")";
        if (value == 0x93BD) return "GL_COMPRESSED_RGBA_ASTC_12x12_KHR (" + hexv(value) + ")";
        if (value == 0x93D0) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4_KHR (" + hexv(value) + ")";
        if (value == 0x93D1) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4_KHR (" + hexv(value) + ")";
        if (value == 0x93D2) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D3) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D4) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6_KHR (" + hexv(value) + ")";
        if (value == 0x93D5) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D6) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x6_KHR (" + hexv(value) + ")";
        if (value == 0x93D7) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_8x8_KHR (" + hexv(value) + ")";
        if (value == 0x93D8) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x5_KHR (" + hexv(value) + ")";
        if (value == 0x93D9) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x6_KHR (" + hexv(value) + ")";
        if (value == 0x93DA) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x8_KHR (" + hexv(value) + ")";
        if (value == 0x93DB) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_10x10_KHR (" + hexv(value) + ")";
        if (value == 0x93DC) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x10_KHR (" + hexv(value) + ")";
        if (value == 0x93DD) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_12x12_KHR (" + hexv(value) + ")";
        if (value == 0x93C0) return "GL_COMPRESSED_RGBA_ASTC_3x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93C1) return "GL_COMPRESSED_RGBA_ASTC_4x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93C2) return "GL_COMPRESSED_RGBA_ASTC_4x4x3_OES (" + hexv(value) + ")";
        if (value == 0x93C3) return "GL_COMPRESSED_RGBA_ASTC_4x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93C4) return "GL_COMPRESSED_RGBA_ASTC_5x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93C5) return "GL_COMPRESSED_RGBA_ASTC_5x5x4_OES (" + hexv(value) + ")";
        if (value == 0x93C6) return "GL_COMPRESSED_RGBA_ASTC_5x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93C7) return "GL_COMPRESSED_RGBA_ASTC_6x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93C8) return "GL_COMPRESSED_RGBA_ASTC_6x6x5_OES (" + hexv(value) + ")";
        if (value == 0x93C9) return "GL_COMPRESSED_RGBA_ASTC_6x6x6_OES (" + hexv(value) + ")";
        if (value == 0x93E0) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_3x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93E1) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x3x3_OES (" + hexv(value) + ")";
        if (value == 0x93E2) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4x3_OES (" + hexv(value) + ")";
        if (value == 0x93E3) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_4x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93E4) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x4x4_OES (" + hexv(value) + ")";
        if (value == 0x93E5) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5x4_OES (" + hexv(value) + ")";
        if (value == 0x93E6) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_5x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93E7) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x5x5_OES (" + hexv(value) + ")";
        if (value == 0x93E8) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6x5_OES (" + hexv(value) + ")";
        if (value == 0x93E9) return "GL_COMPRESSED_SRGB8_ALPHA8_ASTC_6x6x6_OES (" + hexv(value) + ")";
    }
    if (category == "shaderBinaryFormats") {
        if (value == 0x8C0A) return "GL_SGX_BINARY_IMG (" + hexv(value) + ")";
        if (value == 0x8F60) return "GL_MALI_SHADER_BINARY_ARM (" + hexv(value) + ")";
        if (value == 0x8FC4) return "GL_SHADER_BINARY_VIV (" + hexv(value) + ")";
        if (value == 0x9250) return "GL_SHADER_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9260) return "GL_GCCSO_SHADER_BINARY_FJ (" + hexv(value) + ")";
        if (value == 0x9770) return "GL_SHADER_BINARY_HUAWEI (" + hexv(value) + ")";
    }
    if (category == "programBinaryFormats") {
        if (value == 0x8740) return "GL_Z400_BINARY_AMD (" + hexv(value) + ")";
        if (value == 0x8F61) return "GL_MALI_PROGRAM_BINARY_ARM (" + hexv(value) + ")";
        if (value == 0x9130) return "GL_SGX_PROGRAM_BINARY_IMG (" + hexv(value) + ")";
        if (value == 0x93A6) return "GL_PROGRAM_BINARY_ANGLE (" + hexv(value) + ")";
        if (value == 0x875F) return "GL_PROGRAM_BINARY_FORMAT_MESA (" + hexv(value) + ")";
        if (value == 0x9251) return "GL_SMAPHS30_PROGRAM_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9252) return "GL_SMAPHS_PROGRAM_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9253) return "GL_DMP_PROGRAM_BINARY_DMP (" + hexv(value) + ")";
        if (value == 0x9771) return "GL_PROGRAM_BINARY_HUAWEI (" + hexv(value) + ")";
    }
    return hexv(value);
}

static size_t boundedCStringLength(const char* s) {
    if (!s) return 0;
    for (size_t i = 0; i <= kMaxRuntimeStringBytes; ++i) if (s[i] == '\0') return i;
    return kMaxRuntimeStringBytes + 1;
}

static bool runtimeStringValid(const char* s) {
    if (!s) return false;
    const size_t length = boundedCStringLength(s);
    if (length > kMaxRuntimeStringBytes) return false;
    for (size_t i = 0; i < length;) {
        const uint32_t c = static_cast<unsigned char>(s[i]);
        if (c < 0x80u) { ++i; continue; }
        const size_t width = c >= 0xC2u && c <= 0xDFu ? 2 :
                             c >= 0xE0u && c <= 0xEFu ? 3 :
                             c >= 0xF0u && c <= 0xF4u ? 4 : 0;
        if (!width || i + width > length) return false;
        uint32_t cp = width == 2 ? c & 0x1Fu : width == 3 ? c & 0x0Fu : c & 0x07u;
        for (size_t j = 1; j < width; ++j) {
            const uint32_t cc = static_cast<unsigned char>(s[i + j]);
            if ((cc & 0xC0u) != 0x80u) return false;
            cp = (cp << 6) | (cc & 0x3Fu);
        }
        if (cp < (width == 2 ? 0x80u : width == 3 ? 0x800u : 0x10000u) ||
            cp > 0x10FFFFu || (cp >= 0xD800u && cp <= 0xDFFFu)) return false;
        i += width;
    }
    return true;
}

static void normalizeExtensionList(std::vector<std::string>& extensions) {
    std::sort(extensions.begin(), extensions.end());
    extensions.erase(std::unique(extensions.begin(), extensions.end()), extensions.end());
}

static std::vector<std::string> splitExt(const char* s, bool* complete = nullptr) {
    if (complete) *complete = false;
    std::vector<std::string> out;
    if (!runtimeStringValid(s)) return out;
    const size_t length = boundedCStringLength(s);
    size_t i = 0;
    while (i < length) {
        while (i < length && std::isspace(static_cast<unsigned char>(s[i]))) ++i;
        const size_t start = i;
        while (i < length && !std::isspace(static_cast<unsigned char>(s[i]))) ++i;
        const size_t tokenLength = i - start;
        if (tokenLength == 0) continue;
        if (tokenLength > kMaxExtensionTokenBytes || out.size() >= static_cast<size_t>(kMaxGlEnumerationCount)) return {};
        out.emplace_back(s + start, tokenLength);
    }
    const size_t received = out.size();
    normalizeExtensionList(out);
    if (out.size() != received) return {};
    if (complete) *complete = true;
    return out;
}

static bool hasExt(const std::vector<std::string>& extensions, const char* name) {
    return std::binary_search(extensions.begin(), extensions.end(), std::string(name));
}

static void appendStringArray(std::ostringstream& o, const std::vector<std::string>& v) {
    o << '[';
    for (size_t i = 0; i < v.size(); ++i) {
        if (i) o << ',';
        o << q(v[i]);
    }
    o << ']';
}

static std::pair<int, int> parseGlVersion(const char* s) {
    if (!s) return {0, 0};
    const std::string v(s);
    const auto p = v.find("OpenGL ES ");
    if (p == std::string::npos) return {0, 0};
    size_t i = p + 10;
    while (i < v.size() && std::isspace(static_cast<unsigned char>(v[i]))) ++i;
    int major = 0;
    int minor = 0;
    while (i < v.size() && std::isdigit(static_cast<unsigned char>(v[i]))) major = major * 10 + (v[i++] - '0');
    if (i < v.size() && v[i] == '.') ++i;
    while (i < v.size() && std::isdigit(static_cast<unsigned char>(v[i]))) minor = minor * 10 + (v[i++] - '0');
    return {major, minor};
}

static int versionCode(const std::pair<int, int>& v) { return v.first * 100 + v.second * 10; }

static void clearGlErrors() { for (int i = 0; i < 16; ++i) { if (glGetError() == GL_NO_ERROR) return; } }

struct QueryDiagnosticNative { std::string name; std::string status; std::string detail; };
struct EglCapabilityNative { std::string name; std::string status; std::string value; std::string detail; };
struct GlRuntimeNative { std::string name; std::string value; bool available; bool applicable; std::string detail; };
static thread_local std::vector<QueryDiagnosticNative>* activeDiagnostics = nullptr;

class ActiveDiagnosticsScope {
public:
    explicit ActiveDiagnosticsScope(std::vector<QueryDiagnosticNative>& current) : previous_(activeDiagnostics) { activeDiagnostics = &current; }
    ActiveDiagnosticsScope(const ActiveDiagnosticsScope&) = delete;
    ActiveDiagnosticsScope& operator=(const ActiveDiagnosticsScope&) = delete;
    void reset() { if (active_) { activeDiagnostics = previous_; active_ = false; } }
    ~ActiveDiagnosticsScope() { reset(); }
private:
    std::vector<QueryDiagnosticNative>* previous_ = nullptr;
    bool active_ = true;
};

static std::string glErrorHex(GLenum error) { std::ostringstream o; o << "0x" << std::uppercase << std::hex << static_cast<unsigned int>(error); return o.str(); }
static void diagnostic(const char* name, GLenum error) { if (!activeDiagnostics) return; const std::string status = error == GL_NO_ERROR ? "Available" : "Unavailable"; const std::string detail = error == GL_NO_ERROR ? "" : std::string("GL error ") + glErrorHex(error); auto it = std::find_if(activeDiagnostics->begin(), activeDiagnostics->end(), [&](const QueryDiagnosticNative& x) { return x.name == name; }); if (it == activeDiagnostics->end()) { activeDiagnostics->push_back({name, status, detail}); return; } if (it->status == "Available" && status == "Available") return; if (it->status != status || it->detail != detail) { it->status = "Unavailable"; it->detail = detail.empty() ? "Repeated query produced inconsistent runtime evidence" : detail; } }

static void reconcileDiagnosticIdentities(std::vector<QueryDiagnosticNative>& records) {
    std::vector<QueryDiagnosticNative> reconciled;
    reconciled.reserve(records.size());
    for (const auto& entry : records) {
        auto previous = std::find_if(reconciled.begin(), reconciled.end(), [&](const QueryDiagnosticNative& item) { return item.name == entry.name; });
        if (previous == reconciled.end()) {
            reconciled.push_back(entry);
            continue;
        }
        if (previous->status == entry.status && previous->detail == entry.detail) continue;
        previous->status = "Unavailable";
        previous->detail = "Repeated query identity returned inconsistent status or error evidence";
    }
    records.swap(reconciled);
}

static std::string glResetStrategyDisplay(GLint value, const char* queryName) {
    const std::string query = queryName ? queryName : "";
    const char* suffix = query.find("_KHR") != std::string::npos ? "_KHR" : query.find("_EXT") != std::string::npos ? "_EXT" : "";
    if (value == 0x8252) return std::string("GL_LOSE_CONTEXT_ON_RESET") + suffix + " (0x8252)";
    if (value == 0x8261) return std::string("GL_NO_RESET_NOTIFICATION") + suffix + " (0x8261)";
    return hexv(value);
}

static std::string glContextFlagsDisplay(GLint value) {
    std::vector<std::string> bits;
    if ((value & 0x00000002) != 0) bits.push_back("GL_CONTEXT_FLAG_DEBUG_BIT");
    if ((value & 0x00000004) != 0) bits.push_back("GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT");
    if ((value & 0x00000008) != 0) bits.push_back("GL_CONTEXT_FLAG_NO_ERROR_BIT_KHR");
    std::ostringstream out;
    out << hexv(value);
    if (!bits.empty()) {
        out << " (";
        for (size_t i = 0; i < bits.size(); ++i) { if (i) out << " | "; out << bits[i]; }
        out << ')';
    } else if (value == 0) {
        out << " (no context flag bits set)";
    }
    return out.str();
}

static const char* queryGlString(GLenum name, const char* diagnosticName) {
    clearGlErrors();
    const auto* value = reinterpret_cast<const char*>(glGetString(name));
    const GLenum error = glGetError();
    const bool valid = error == GL_NO_ERROR && runtimeStringValid(value);
    diagnostic(diagnosticName, valid ? GL_NO_ERROR : error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
    return valid ? value : nullptr;
}

static std::pair<int, int> runtimeGlVersion(const char* text) {
    const auto parsed = parseGlVersion(text);
    if (versionCode(parsed) < 300) return parsed;
    GLint major = 0;
    GLint minor = 0;
    clearGlErrors();
    glGetIntegerv(GL_MAJOR_VERSION, &major);
    const GLenum majorError = glGetError();
    diagnostic("GL_MAJOR_VERSION", majorError);
    clearGlErrors();
    glGetIntegerv(GL_MINOR_VERSION, &minor);
    const GLenum minorError = glGetError();
    diagnostic("GL_MINOR_VERSION", minorError);
    if (majorError == GL_NO_ERROR && minorError == GL_NO_ERROR) {
        if (major != parsed.first || minor != parsed.second) {
            diagnostic("Runtime GL version consistency", GL_INVALID_VALUE);
            return {0, -1};
        }
        diagnostic("Runtime GL version consistency", GL_NO_ERROR);
        return {major, minor};
    }
    return parsed;
}


static void addLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v = 0;
    clearGlErrors();
    glGetIntegerv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static void addLimit2(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v[2] = {0, 0};
    clearGlErrors();
    glGetIntegerv(e, v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v[0]) + " × " + std::to_string(v[1])) << '}';
}

static void addFloatLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLfloat v = 0.0f;
    clearGlErrors();
    glGetFloatv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR || !std::isfinite(v)) {
        if (error == GL_NO_ERROR) diagnostic(name, GL_INVALID_VALUE);
        return;
    }
    if (!first) o << ',';
    first = false;
    std::ostringstream s;
    s << std::setprecision(std::numeric_limits<GLfloat>::max_digits10) << v;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(s.str()) << '}';
}

static void addFloatLimit2(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLfloat v[2] = {0.0f, 0.0f};
    clearGlErrors();
    glGetFloatv(e, v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR || !std::isfinite(v[0]) || !std::isfinite(v[1])) {
        if (error == GL_NO_ERROR) diagnostic(name, GL_INVALID_VALUE);
        return;
    }
    if (!first) o << ',';
    first = false;
    std::ostringstream s;
    s << std::setprecision(std::numeric_limits<GLfloat>::max_digits10) << v[0] << " … " << v[1];
    o << "{\"name\":" << q(name) << ",\"value\":" << q(s.str()) << '}';
}

static void addBooleanLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLboolean v = GL_FALSE;
    clearGlErrors();
    glGetBooleanv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(v == GL_TRUE ? "True" : "False") << '}';
}

static void addHexLimit(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint v = 0;
    clearGlErrors();
    glGetIntegerv(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(hexv(v)) << '}';
}

static void addLimit64(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint64 v = 0;
    clearGlErrors();
    glGetInteger64v(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static void addHexLimit64(std::ostringstream& o, bool& first, const char* name, GLenum e) {
    GLint64 v = 0;
    clearGlErrors();
    glGetInteger64v(e, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    std::ostringstream value;
    value << "0x" << std::uppercase << std::hex << static_cast<GLuint64>(v);
    o << "{\"name\":" << q(name) << ",\"value\":" << q(value.str()) << '}';
}

static void addIndexedLimit(std::ostringstream& o, bool& first, const char* name, GLenum e, GLuint index) {
    GLint v = 0;
    clearGlErrors();
    glGetIntegeri_v(e, index, &v);
    const GLenum error = glGetError();
    const std::string indexedName = std::string(name) + "[" + std::to_string(index) + "]";
    diagnostic(indexedName.c_str(), error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(indexedName) << ",\"value\":" << q(std::to_string(v)) << '}';
}

using GetQueryivExtProc = void (*)(GLenum, GLenum, GLint*);
using GetFragmentShadingRatesExtProc = void (*)(GLsizei, GLsizei, GLsizei*, GLenum*);
using GetInternalformatSampleivNvProc = void (*)(GLenum, GLenum, GLsizei, GLenum, GLsizei, GLint*);

struct InternalFormatDef { GLenum value; const char* name; };
static constexpr InternalFormatDef kRenderableInternalFormats[] = {
    {GL_R8, "GL_R8"}, {GL_RG8, "GL_RG8"}, {GL_RGB8, "GL_RGB8"}, {GL_RGB565, "GL_RGB565"},
    {GL_RGBA4, "GL_RGBA4"}, {GL_RGB5_A1, "GL_RGB5_A1"}, {GL_RGBA8, "GL_RGBA8"}, {GL_RGB10_A2, "GL_RGB10_A2"},
    {GL_RGB10_A2UI, "GL_RGB10_A2UI"}, {GL_SRGB8_ALPHA8, "GL_SRGB8_ALPHA8"}, {GL_R16F, "GL_R16F"},
    {GL_RG16F, "GL_RG16F"}, {GL_RGBA16F, "GL_RGBA16F"}, {GL_R32F, "GL_R32F"}, {GL_RG32F, "GL_RG32F"},
    {GL_RGBA32F, "GL_RGBA32F"}, {GL_R11F_G11F_B10F, "GL_R11F_G11F_B10F"}, {GL_R8I, "GL_R8I"}, {GL_R8UI, "GL_R8UI"},
    {GL_R16I, "GL_R16I"}, {GL_R16UI, "GL_R16UI"}, {GL_R32I, "GL_R32I"}, {GL_R32UI, "GL_R32UI"},
    {GL_RG8I, "GL_RG8I"}, {GL_RG8UI, "GL_RG8UI"}, {GL_RG16I, "GL_RG16I"}, {GL_RG16UI, "GL_RG16UI"},
    {GL_RG32I, "GL_RG32I"}, {GL_RG32UI, "GL_RG32UI"}, {GL_RGBA8I, "GL_RGBA8I"}, {GL_RGBA8UI, "GL_RGBA8UI"},
    {GL_RGBA16I, "GL_RGBA16I"}, {GL_RGBA16UI, "GL_RGBA16UI"}, {GL_RGBA32I, "GL_RGBA32I"}, {GL_RGBA32UI, "GL_RGBA32UI"},
    {GL_DEPTH_COMPONENT16, "GL_DEPTH_COMPONENT16"}, {GL_DEPTH_COMPONENT24, "GL_DEPTH_COMPONENT24"},
    {GL_DEPTH_COMPONENT32F, "GL_DEPTH_COMPONENT32F"}, {GL_DEPTH24_STENCIL8, "GL_DEPTH24_STENCIL8"},
    {GL_DEPTH32F_STENCIL8, "GL_DEPTH32F_STENCIL8"}, {GL_STENCIL_INDEX8, "GL_STENCIL_INDEX8"}
};

static void appendInternalFormatEntry(
    std::ostringstream& o,
    bool& first,
    GLenum target,
    const char* targetName,
    const InternalFormatDef& format,
    GetInternalformatSampleivNvProc nvQuery
) {
    GLint count = 0;
    clearGlErrors();
    glGetInternalformativ(target, format.value, GL_NUM_SAMPLE_COUNTS, 1, &count);
    GLenum error = glGetError();
    const std::string queryName = std::string("glGetInternalformativ/") + targetName + "/" + format.name;
    std::string status = "Available";
    std::string detail;
    std::vector<GLint> samples;
    if (error != GL_NO_ERROR) {
        status = "Unavailable";
        detail = std::string("GL_NUM_SAMPLE_COUNTS failed with GL error ") + glErrorHex(error);
    } else if (count < 0 || count > kMaxInternalFormatSampleCounts) {
        status = "Unavailable";
        detail = "GL_NUM_SAMPLE_COUNTS exceeded the 64-entry safety bound";
    } else if (count > 0) {
        samples.resize(static_cast<size_t>(count));
        clearGlErrors();
        glGetInternalformativ(target, format.value, GL_SAMPLES, count, samples.data());
        error = glGetError();
        if (error != GL_NO_ERROR) {
            status = "Unavailable";
            detail = std::string("GL_SAMPLES failed with GL error ") + glErrorHex(error);
            samples.clear();
        } else {
            std::vector<GLint> observedSamples;
            for (GLint sample : samples) {
                if (sample <= 0 || std::find(observedSamples.begin(), observedSamples.end(), sample) != observedSamples.end()) {
                    status = "Unavailable";
                    detail = sample <= 0 ? "GL_SAMPLES returned a non-positive sample count" : "GL_SAMPLES returned a duplicate sample count; this internal format is not reported as complete";
                    samples.clear();
                    break;
                }
                observedSamples.push_back(sample);
            }
        }
    }
    if (activeDiagnostics) activeDiagnostics->push_back({queryName, status, detail});
    if (!first) o << ',';
    first = false;
    o << "{\"target\":" << q(targetName)
      << ",\"internalFormat\":" << q(format.name)
      << ",\"status\":" << q(status)
      << ",\"detail\":" << q(detail)
      << ",\"sampleCounts\":[";
    for (size_t i = 0; i < samples.size(); ++i) {
        if (i) o << ',';
        o << samples[i];
    }
    o << "],\"nvSampleProperties\":[";
    bool nvFirst = true;
    if (status == "Available" && nvQuery) {
        for (GLint sample : samples) {
            GLint multisamples = 0, scaleX = 0, scaleY = 0, conformant = 0;
            const struct { GLenum pname; GLint* out; } queries[] = {
                {0x9371, &multisamples}, {0x9372, &scaleX}, {0x9373, &scaleY}, {0x9374, &conformant}
            };
            bool ok = true;
            GLenum nvError = GL_NO_ERROR;
            for (const auto& query : queries) {
                clearGlErrors();
                nvQuery(target, format.value, sample, query.pname, 1, query.out);
                nvError = glGetError();
                if (nvError != GL_NO_ERROR) { ok = false; break; }
            }
            const std::string nvName = queryName + "/NV(samples=" + std::to_string(sample) + ")";
            if (activeDiagnostics) activeDiagnostics->push_back({nvName, ok ? "Available" : "Unavailable", ok ? "" : std::string("GL error ") + glErrorHex(nvError)});
            if (!ok) continue;
            if (!nvFirst) o << ',';
            nvFirst = false;
            o << "{\"samples\":" << sample
              << ",\"multisamples\":" << multisamples
              << ",\"supersampleScaleX\":" << scaleX
              << ",\"supersampleScaleY\":" << scaleY
              << ",\"conformant\":" << (conformant == GL_TRUE ? "true" : "false") << '}';
        }
    }
    o << "]}";
}

static std::string fragmentShadingRateDisplay(GLenum value) {
    switch (value) {
        case 0x96A6: return "GL_SHADING_RATE_1X1_PIXELS_EXT (0x96A6)";
        case 0x96A7: return "GL_SHADING_RATE_1X2_PIXELS_EXT (0x96A7)";
        case 0x96AA: return "GL_SHADING_RATE_1X4_PIXELS_EXT (0x96AA)";
        case 0x96A8: return "GL_SHADING_RATE_2X1_PIXELS_EXT (0x96A8)";
        case 0x96A9: return "GL_SHADING_RATE_2X2_PIXELS_EXT (0x96A9)";
        case 0x96AD: return "GL_SHADING_RATE_2X4_PIXELS_EXT (0x96AD)";
        case 0x96AB: return "GL_SHADING_RATE_4X1_PIXELS_EXT (0x96AB)";
        case 0x96AC: return "GL_SHADING_RATE_4X2_PIXELS_EXT (0x96AC)";
        case 0x96AE: return "GL_SHADING_RATE_4X4_PIXELS_EXT (0x96AE)";
        default: return hexv(static_cast<EGLint>(value));
    }
}

static void addFragmentShadingRates(std::ostringstream& o, bool& first, GetFragmentShadingRatesExtProc fn, GLsizei samples) {
    const std::string name = "glGetFragmentShadingRatesEXT(samples=" + std::to_string(samples) + ")";
    if (!fn) {
        if (activeDiagnostics) activeDiagnostics->push_back({name, "Unavailable", "glGetFragmentShadingRatesEXT unavailable"});
        return;
    }
    GLsizei count = 0;
    clearGlErrors();
    fn(samples, 0, &count, nullptr);
    GLenum error = glGetError();
    if (error != GL_NO_ERROR || count < 0 || count > 64) {
        if (activeDiagnostics) activeDiagnostics->push_back({name, "Unavailable", error == GL_NO_ERROR ? "Returned shading-rate count exceeded the 64-entry safety bound" : std::string("GL error ") + glErrorHex(error)});
        return;
    }
    std::vector<GLenum> rates(static_cast<size_t>(count));
    GLsizei written = 0;
    if (count > 0) {
        clearGlErrors();
        fn(samples, count, &written, rates.data());
        error = glGetError();
        if (error != GL_NO_ERROR || written < 0 || written > count) {
            if (activeDiagnostics) activeDiagnostics->push_back({name, "Unavailable", error == GL_NO_ERROR ? "Returned shading-rate result count exceeded the allocated query result" : std::string("GL error ") + glErrorHex(error)});
            return;
        }
        rates.resize(static_cast<size_t>(written));
    }
    if (activeDiagnostics) activeDiagnostics->push_back({name, "Available", ""});
    std::ostringstream value;
    for (size_t i = 0; i < rates.size(); ++i) {
        if (i) value << ", ";
        value << fragmentShadingRateDisplay(rates[i]);
    }
    if (rates.empty()) value << "None reported";
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(value.str()) << '}';
}

static void addQueryCounterBitsExt(std::ostringstream& o, bool& first, GetQueryivExtProc fn, const char* name, GLenum target) {
    if (!fn) {
        if (activeDiagnostics) activeDiagnostics->push_back({name, "Unavailable", "glGetQueryivEXT unavailable"});
        return;
    }
    GLint v = 0;
    clearGlErrors();
    fn(target, 0x8864, &v);
    const GLenum error = glGetError();
    diagnostic(name, error);
    if (error != GL_NO_ERROR) return;
    if (!first) o << ',';
    first = false;
    o << "{\"name\":" << q(name) << ",\"value\":" << q(std::to_string(v)) << '}';
}

static std::vector<std::string> glExtensions(int glCode, bool& complete) {
    complete = false;
    std::vector<std::string> out;
    if (glCode >= 300) {
        GLint n = 0;
        clearGlErrors();
        glGetIntegerv(GL_NUM_EXTENSIONS, &n);
        GLenum error = glGetError();
        diagnostic("GL_NUM_EXTENSIONS", error == GL_NO_ERROR && n >= 0 && n <= kMaxGlEnumerationCount ? GL_NO_ERROR : error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
        if (error != GL_NO_ERROR || n < 0 || n > kMaxGlEnumerationCount) {
            diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
            return out;
        }
        out.reserve(static_cast<size_t>(n));
        for (GLint i = 0; i < n; ++i) {
            clearGlErrors();
            const auto* extension = reinterpret_cast<const char*>(glGetStringi(GL_EXTENSIONS, static_cast<GLuint>(i)));
            error = glGetError();
            if (error != GL_NO_ERROR || !runtimeStringValid(extension)) {
                out.clear();
                diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
                return out;
            }
            const size_t extensionLength = boundedCStringLength(extension);
            if (extensionLength == 0 || extensionLength > kMaxExtensionTokenBytes ||
                std::any_of(extension, extension + extensionLength, [](unsigned char byte) { return std::isspace(byte) != 0; })) {
                out.clear();
                diagnostic("GL_EXTENSIONS", GL_INVALID_VALUE);
                return out;
            }
            if (*extension == '\0') {
                out.clear();
                diagnostic("GL_EXTENSIONS", GL_INVALID_VALUE);
                return out;
            }
            out.emplace_back(extension, extensionLength);
        }
        const size_t received = out.size();
        normalizeExtensionList(out);
        if (out.size() != received) {
            diagnostic("GL_EXTENSIONS", GL_INVALID_VALUE);
            return {};
        }
        diagnostic("GL_EXTENSIONS", GL_NO_ERROR);
        complete = true;
    } else {
        clearGlErrors();
        const auto* extensionString = reinterpret_cast<const char*>(glGetString(GL_EXTENSIONS));
        const GLenum error = glGetError();
        if (error != GL_NO_ERROR || !runtimeStringValid(extensionString)) {
            diagnostic("GL_EXTENSIONS", error == GL_NO_ERROR ? GL_INVALID_VALUE : error);
            return out;
        }
        out = splitExt(extensionString, &complete);
        if (!complete) {
            diagnostic("GL_EXTENSIONS", GL_INVALID_VALUE);
            return {};
        }
        diagnostic("GL_EXTENSIONS", GL_NO_ERROR);
    }
    return out;
}

static bool chooseConfig(EGLDisplay d, EGLint renderableBit, EGLConfig& cfg) {
    const EGLint preferred[] = {
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, renderableBit,
        EGL_RED_SIZE, 8,
        EGL_GREEN_SIZE, 8,
        EGL_BLUE_SIZE, 8,
        EGL_ALPHA_SIZE, 8,
        EGL_NONE
    };
    const EGLint minimal[] = {
        EGL_SURFACE_TYPE, EGL_PBUFFER_BIT,
        EGL_RENDERABLE_TYPE, renderableBit,
        EGL_NONE
    };
    EGLint count = 0;
    cfg = nullptr;
    if (eglChooseConfig(d, preferred, &cfg, 1, &count) == EGL_TRUE && count > 0 && cfg != nullptr) return true;
    count = 0;
    cfg = nullptr;
    return eglChooseConfig(d, minimal, &cfg, 1, &count) == EGL_TRUE && count > 0 && cfg != nullptr;
}

struct ContextCreationInfo {
    int requestedMajor = 0;
    int requestedMinor = 0;
    EGLint renderableBit = 0;
    std::string path;
};

static EGLContext createBestContext(EGLDisplay d, EGLConfig& cfg, int eglCode, const std::vector<std::string>& displayExt, ContextCreationInfo* creationInfo = nullptr) {
    if (creationInfo) *creationInfo = {};
    if (eglBindAPI(EGL_OPENGL_ES_API) != EGL_TRUE) return EGL_NO_CONTEXT;
    const bool hasKhrCreateContext = hasExt(displayExt, "EGL_KHR_create_context");
    const bool canCreateEs3 = eglCode >= 150 || hasKhrCreateContext;
    if (canCreateEs3 && chooseConfig(d, EGL_OPENGL_ES3_BIT_KHR, cfg)) {
        const EGLint versions[][5] = {
            {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 2, EGL_NONE},
            {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 1, EGL_NONE},
            {EGL_CONTEXT_MAJOR_VERSION_KHR, 3, EGL_CONTEXT_MINOR_VERSION_KHR, 0, EGL_NONE}
        };
        const int minors[] = {2, 1, 0};
        for (size_t i = 0; i < 3; ++i) {
            EGLContext c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, versions[i]);
            if (c != EGL_NO_CONTEXT) {
                if (creationInfo) {
                    creationInfo->requestedMajor = 3;
                    creationInfo->requestedMinor = minors[i];
                    creationInfo->renderableBit = EGL_OPENGL_ES3_BIT_KHR;
                    creationInfo->path = eglCode >= 150 ? "EGL 1.5 major/minor context attributes" : "EGL_KHR_create_context major/minor attributes";
                }
                return c;
            }
        }
    }
    if (chooseConfig(d, EGL_OPENGL_ES2_BIT, cfg)) {
        const EGLint es2Attrs[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
        EGLContext c = eglCreateContext(d, cfg, EGL_NO_CONTEXT, es2Attrs);
        if (c != EGL_NO_CONTEXT && creationInfo) {
            creationInfo->requestedMajor = 2;
            creationInfo->requestedMinor = 0;
            creationInfo->renderableBit = EGL_OPENGL_ES2_BIT;
            creationInfo->path = "EGL core OpenGL ES 2 fallback";
        }
        return c;
    }
    return EGL_NO_CONTEXT;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_efishell_openglesscope_OpenGLESProbeService_nativeCollect(JNIEnv* env, jobject) {
    EGLDisplay d = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (d == EGL_NO_DISPLAY) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display unavailable\"}"); }

    EGLint eglMajor = 0;
    EGLint eglMinor = 0;
    if (eglInitialize(d, &eglMajor, &eglMinor) != EGL_TRUE) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglInitialize failed\"}"); }

    EGLConfig cfg = nullptr;
    eglGetError();
    const char* rawDisplayExtensionText = eglQueryString(d, EGL_EXTENSIONS);
    const EGLint displayExtensionError = eglGetError();
    const bool displayExtensionTextValid = displayExtensionError == EGL_SUCCESS && runtimeStringValid(rawDisplayExtensionText);
    const char* displayExtensionText = displayExtensionTextValid ? rawDisplayExtensionText : nullptr;
    bool displayExtComplete = false;
    const auto displayExt = splitExt(displayExtensionText, &displayExtComplete);
    if (!displayExtComplete) {
        releaseEgl(d, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display extension enumeration was missing, oversized or incomplete\"}");
    }
    const int eglCode = eglMajor * 100 + eglMinor * 10;
    ContextCreationInfo contextCreation;
    EGLContext c = createBestContext(d, cfg, eglCode, displayExt, &contextCreation);
    if (c == EGL_NO_CONTEXT || cfg == nullptr) {
        releaseEgl(d, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES context creation failed\"}");
    }

    const EGLint pbAttrs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface s = eglCreatePbufferSurface(d, cfg, pbAttrs);
    if (s == EGL_NO_SURFACE) {
        releaseEgl(d, EGL_NO_SURFACE, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL pbuffer creation failed\"}");
    }
    if (eglMakeCurrent(d, s, s, c) != EGL_TRUE) {
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglMakeCurrent failed\"}");
    }

    std::vector<QueryDiagnosticNative> diagnostics;
    ActiveDiagnosticsScope diagnosticsScope(diagnostics);
    const char* glVendor = queryGlString(GL_VENDOR, "GL_VENDOR");
    const char* glRenderer = queryGlString(GL_RENDERER, "GL_RENDERER");
    const char* glVersion = queryGlString(GL_VERSION, "GL_VERSION");
    const char* glslVersion = queryGlString(GL_SHADING_LANGUAGE_VERSION, "GL_SHADING_LANGUAGE_VERSION");
    if (glVendor == nullptr || glRenderer == nullptr || glVersion == nullptr || glslVersion == nullptr) {
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Required OpenGL ES identity strings were unavailable\"}");
    }
    const auto parsed = runtimeGlVersion(glVersion);
    if (parsed.first < 2 || parsed.second < 0 || parsed.second > 9) {
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"The runtime OpenGL ES version could not be established safely\"}");
    }
    const int glCode = versionCode(parsed);
    bool glExtComplete = false;
    const auto glExt = glExtensions(glCode, glExtComplete);
    if (!glExtComplete) {
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"GL extension enumeration failed or exceeded a safety bound; an empty supported-extension list was not fabricated\"}");
    }

    auto queryGlRuntimeInt = [&](const char* name, GLenum token, bool applicable, const char* notApplicableDetail, bool asFlags = false, bool asReset = false) -> GlRuntimeNative {
        if (!applicable) {
            diagnostics.push_back({name, "Not applicable", notApplicableDetail});
            return {name, "", false, false, notApplicableDetail};
        }
        GLint value = 0;
        clearGlErrors();
        glGetIntegerv(token, &value);
        const GLenum error = glGetError();
        diagnostic(name, error);
        if (error != GL_NO_ERROR) return {name, "", false, true, std::string("GL error ") + glErrorHex(error)};
        return {name, asFlags ? glContextFlagsDisplay(value) : asReset ? glResetStrategyDisplay(value, name) : std::to_string(value), true, true, ""};
    };
    auto queryGlRuntimeBool = [&](const char* name, GLenum token, bool applicable, const char* notApplicableDetail) -> GlRuntimeNative {
        if (!applicable) {
            diagnostics.push_back({name, "Not applicable", notApplicableDetail});
            return {name, "", false, false, notApplicableDetail};
        }
        GLboolean value = GL_FALSE;
        clearGlErrors();
        glGetBooleanv(token, &value);
        const GLenum error = glGetError();
        diagnostic(name, error);
        if (error != GL_NO_ERROR) return {name, "", false, true, std::string("GL error ") + glErrorHex(error)};
        return {name, value == GL_TRUE ? "True" : "False", true, true, ""};
    };
    GLint glContextFlagsRaw = 0;
    const bool contextFlagsApplicable = glCode >= 320;
    const auto glContextFlags = [&]() -> GlRuntimeNative {
        if (!contextFlagsApplicable) {
            diagnostics.push_back({"GL_CONTEXT_FLAGS", "Not applicable", "Requires OpenGL ES 3.2+"});
            return {"GL_CONTEXT_FLAGS", "", false, false, "Requires OpenGL ES 3.2+"};
        }
        clearGlErrors();
        glGetIntegerv(GL_CONTEXT_FLAGS, &glContextFlagsRaw);
        const GLenum error = glGetError();
        diagnostic("GL_CONTEXT_FLAGS", error);
        if (error != GL_NO_ERROR) return GlRuntimeNative{"GL_CONTEXT_FLAGS", "", false, true, std::string("GL error ") + glErrorHex(error)};
        return GlRuntimeNative{"GL_CONTEXT_FLAGS", glContextFlagsDisplay(glContextFlagsRaw), true, true, ""};
    }();
    const bool resetCoreApplicable = glCode >= 320;
    const bool resetKhrApplicable = !resetCoreApplicable && hasExt(glExt, "GL_KHR_robustness");
    const bool resetExtApplicable = !resetCoreApplicable && !resetKhrApplicable && hasExt(glExt, "GL_EXT_robustness");
    const auto glResetStrategy = resetCoreApplicable
        ? queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY", 0x8256, true, "", false, true)
        : resetKhrApplicable
            ? queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_KHR", 0x8256, resetKhrApplicable, "", false, true)
            : queryGlRuntimeInt("GL_RESET_NOTIFICATION_STRATEGY_EXT", 0x8256, resetExtApplicable, "Requires OpenGL ES 3.2+, GL_KHR_robustness, or GL_EXT_robustness", false, true);
    const auto glRobustAccess = [&]() -> GlRuntimeNative {
        if (resetCoreApplicable) {
            if (!glContextFlags.available) {
                diagnostics.push_back({"GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "Unavailable", glContextFlags.detail});
                return {"GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "", false, true, glContextFlags.detail};
            }
            const bool enabled = (glContextFlagsRaw & 0x00000004) != 0;
            diagnostics.push_back({"GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", "Available", "Derived from GL_CONTEXT_FLAGS"});
            return {"GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT", enabled ? "True" : "False", true, true, ""};
        }
        if (resetKhrApplicable) return queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_KHR", 0x90F3, resetKhrApplicable, "");
        return queryGlRuntimeBool("GL_CONTEXT_ROBUST_ACCESS_EXT", 0x90F3, resetExtApplicable, "Requires OpenGL ES 3.2+, GL_KHR_robustness, or GL_EXT_robustness");
    }();
    const std::string glRobustAccessQuery = resetCoreApplicable
        ? "GL_CONTEXT_FLAGS / GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT"
        : glRobustAccess.applicable ? glRobustAccess.name : "";
    eglGetError();
    const char* rawClientExtensionText = eglQueryString(EGL_NO_DISPLAY, EGL_EXTENSIONS);
    const EGLint clientExtError = eglGetError();
    const char* clientExtensionText = clientExtError == EGL_SUCCESS && runtimeStringValid(rawClientExtensionText) ? rawClientExtensionText : nullptr;
    bool clientExtComplete = false;
    const auto clientExt = splitExt(clientExtensionText, &clientExtComplete);
    if (clientExtensionText && !clientExtComplete) {
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL client extension enumeration exceeded a safety bound; zero extensions was not assumed\"}");
    }
    auto queryEglDisplayString = [&](EGLint token, const char*& raw, EGLint& error) -> const char* {
        eglGetError();
        raw = eglQueryString(d, token);
        error = eglGetError();
        return error == EGL_SUCCESS && runtimeStringValid(raw) ? raw : nullptr;
    };
    const char* rawEglVendorText = nullptr;
    const char* rawEglVersionText = nullptr;
    const char* rawEglClientApisText = nullptr;
    EGLint eglVendorError = EGL_SUCCESS;
    EGLint eglVersionError = EGL_SUCCESS;
    EGLint eglClientApisError = EGL_SUCCESS;
    const char* eglVendorText = queryEglDisplayString(EGL_VENDOR, rawEglVendorText, eglVendorError);
    const char* eglVersionText = queryEglDisplayString(EGL_VERSION, rawEglVersionText, eglVersionError);
    const char* eglClientApisText = queryEglDisplayString(EGL_CLIENT_APIS, rawEglClientApisText, eglClientApisError);
    auto eglStringDetail = [](const char* raw, const char* valid, EGLint error) {
        if (valid) return std::string();
        if (error != EGL_SUCCESS) return eglErrorDisplay(error);
        return raw ? std::string("EGL runtime string was oversized or invalid UTF-8") : std::string("eglQueryString returned null");
    };
    const bool clientDiscoveryNotApplicable = eglCode < 150 && clientExtError == EGL_BAD_DISPLAY && rawClientExtensionText == nullptr;
    diagnostics.push_back({"EGL_EXTENSIONS", displayExtensionText ? "Available" : "Unavailable", eglStringDetail(rawDisplayExtensionText, displayExtensionText, displayExtensionError)});
    diagnostics.push_back({"EGL_NO_DISPLAY/EGL_EXTENSIONS", clientExtComplete ? "Available" : clientDiscoveryNotApplicable ? "Not applicable" : "Unavailable",
        clientExtComplete ? "" : clientDiscoveryNotApplicable ? "EGL_NO_DISPLAY client extension query is not provided on this legacy implementation" : eglStringDetail(rawClientExtensionText, clientExtensionText, clientExtError)});
    diagnostics.push_back({"EGL_VENDOR", eglVendorText ? "Available" : "Unavailable", eglStringDetail(rawEglVendorText, eglVendorText, eglVendorError)});
    diagnostics.push_back({"EGL_VERSION", eglVersionText ? "Available" : "Unavailable", eglStringDetail(rawEglVersionText, eglVersionText, eglVersionError)});
    diagnostics.push_back({"EGL_CLIENT_APIS", eglClientApisText ? "Available" : eglCode < 120 ? "Not applicable" : "Unavailable", eglCode < 120 ? "Requires EGL 1.2" : eglStringDetail(rawEglClientApisText, eglClientApisText, eglClientApisError)});
    if (!eglVendorText || !eglVersionText || (eglCode >= 120 && !eglClientApisText)) {
        const std::string reason = !eglVendorText ? "Required EGL_VENDOR identity query failed: " + eglStringDetail(rawEglVendorText, eglVendorText, eglVendorError)
            : !eglVersionText ? "Required EGL_VERSION identity query failed: " + eglStringDetail(rawEglVersionText, eglVersionText, eglVersionError)
            : "Required EGL_CLIENT_APIS identity query failed: " + eglStringDetail(rawEglClientApisText, eglClientApisText, eglClientApisError);
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF((std::string("{\"status\":\"unavailable\",\"reason\":") + q(reason) + "}").c_str());
    }

    const EGLenum boundApi = eglQueryAPI();
    const bool currentContext = eglGetCurrentContext() == c;
    const bool currentDisplay = eglGetCurrentDisplay() == d;
    const bool currentDrawSurface = eglGetCurrentSurface(EGL_DRAW) == s;
    const bool currentReadSurface = eglGetCurrentSurface(EGL_READ) == s;
    const bool contextClientTypeApplicable = eglCode >= 120;
    const bool contextClientVersionApplicable = eglCode >= 130;
    const bool contextRenderBufferApplicable = eglCode >= 120;
    const bool surfaceTextureApplicable = eglCode >= 110;
    const bool surfaceResolutionApplicable = eglCode >= 120;
    const bool surfaceVgApplicable = eglCode >= 120;
    const bool surfaceMultisampleApplicable = eglCode >= 140;
    const bool surfaceGlColorspaceApplicable = eglCode >= 150 || hasExt(displayExt, "EGL_KHR_gl_colorspace");

    const auto notQueried = EglAttrResult{0, false, EGL_SUCCESS};
    const auto contextConfigId = queryContextAttr(d, c, EGL_CONFIG_ID);
    const auto contextClientType = contextClientTypeApplicable ? queryContextAttr(d, c, EGL_CONTEXT_CLIENT_TYPE) : notQueried;
    const auto contextClientVersion = contextClientVersionApplicable ? queryContextAttr(d, c, EGL_CONTEXT_CLIENT_VERSION) : notQueried;
    const auto contextRenderBuffer = contextRenderBufferApplicable ? queryContextAttr(d, c, EGL_RENDER_BUFFER) : notQueried;

    const char* surfaceGlColorspaceQuery = !surfaceGlColorspaceApplicable ? nullptr
        : eglCode >= 150 ? "EGL_GL_COLORSPACE (EGL 1.5 core)"
        : "EGL_GL_COLORSPACE_KHR (EGL_KHR_gl_colorspace)";
    const char* surfaceVgAlphaFormatQuery = !surfaceVgApplicable ? nullptr
        : eglCode >= 130 ? "EGL_VG_ALPHA_FORMAT (EGL 1.3+ canonical)"
        : "EGL_ALPHA_FORMAT (EGL 1.2 alias of EGL_VG_ALPHA_FORMAT)";
    const char* surfaceVgColorspaceQuery = !surfaceVgApplicable ? nullptr
        : eglCode >= 130 ? "EGL_VG_COLORSPACE (EGL 1.3+ canonical)"
        : "EGL_COLORSPACE (EGL 1.2 alias of EGL_VG_COLORSPACE)";
    const auto surfaceGlColorspace = surfaceGlColorspaceApplicable ? querySurfaceAttr(d, s, EGL_GL_COLORSPACE) : notQueried;
    const auto surfaceVgAlphaFormat = surfaceVgApplicable ? querySurfaceAttr(d, s, EGL_VG_ALPHA_FORMAT) : notQueried;
    const auto surfaceVgColorspace = surfaceVgApplicable ? querySurfaceAttr(d, s, EGL_VG_COLORSPACE) : notQueried;
    const auto surfaceConfigId = querySurfaceAttr(d, s, EGL_CONFIG_ID);
    const auto surfaceWidth = querySurfaceAttr(d, s, EGL_WIDTH);
    const auto surfaceHeight = querySurfaceAttr(d, s, EGL_HEIGHT);
    const auto surfaceHorizontalResolution = surfaceResolutionApplicable ? querySurfaceAttr(d, s, EGL_HORIZONTAL_RESOLUTION) : notQueried;
    const auto surfaceLargestPbuffer = querySurfaceAttr(d, s, EGL_LARGEST_PBUFFER);
    const auto surfaceMipmapTexture = surfaceTextureApplicable ? querySurfaceAttr(d, s, EGL_MIPMAP_TEXTURE) : notQueried;
    const auto surfaceMipmapLevel = surfaceTextureApplicable ? querySurfaceAttr(d, s, EGL_MIPMAP_LEVEL) : notQueried;
    const auto surfaceMultisampleResolve = surfaceMultisampleApplicable ? querySurfaceAttr(d, s, EGL_MULTISAMPLE_RESOLVE) : notQueried;
    const auto surfacePixelAspectRatio = surfaceResolutionApplicable ? querySurfaceAttr(d, s, EGL_PIXEL_ASPECT_RATIO) : notQueried;
    const auto surfaceRenderBuffer = surfaceResolutionApplicable ? querySurfaceAttr(d, s, EGL_RENDER_BUFFER) : notQueried;
    const auto surfaceSwapBehavior = surfaceResolutionApplicable ? querySurfaceAttr(d, s, EGL_SWAP_BEHAVIOR) : notQueried;
    const auto surfaceTextureFormat = surfaceTextureApplicable ? querySurfaceAttr(d, s, EGL_TEXTURE_FORMAT) : notQueried;
    const auto surfaceTextureTarget = surfaceTextureApplicable ? querySurfaceAttr(d, s, EGL_TEXTURE_TARGET) : notQueried;
    const auto surfaceVerticalResolution = surfaceResolutionApplicable ? querySurfaceAttr(d, s, EGL_VERTICAL_RESOLUTION) : notQueried;

    const EglRuntimeQuery eglRuntimeAttributes[] = {
        {"EGL_CONFIG_ID/context", contextConfigId, true, ""},
        {"EGL_CONTEXT_CLIENT_TYPE", contextClientType, contextClientTypeApplicable, "Requires EGL 1.2+"},
        {"EGL_CONTEXT_CLIENT_VERSION", contextClientVersion, contextClientVersionApplicable, "Requires EGL 1.3+"},
        {"EGL_RENDER_BUFFER/context", contextRenderBuffer, contextRenderBufferApplicable, "Requires EGL 1.2+"},
        {"EGL_GL_COLORSPACE", surfaceGlColorspace, surfaceGlColorspaceApplicable, "Requires EGL 1.5+ or EGL_KHR_gl_colorspace"},
        {"EGL_VG_ALPHA_FORMAT", surfaceVgAlphaFormat, surfaceVgApplicable, "Requires EGL 1.2+ (EGL_ALPHA_FORMAT alias before EGL 1.3)"},
        {"EGL_VG_COLORSPACE", surfaceVgColorspace, surfaceVgApplicable, "Requires EGL 1.2+ (EGL_COLORSPACE alias before EGL 1.3)"},
        {"EGL_CONFIG_ID/surface", surfaceConfigId, true, ""},
        {"EGL_WIDTH", surfaceWidth, true, ""}, {"EGL_HEIGHT", surfaceHeight, true, ""},
        {"EGL_HORIZONTAL_RESOLUTION", surfaceHorizontalResolution, surfaceResolutionApplicable, "Requires EGL 1.2+"},
        {"EGL_LARGEST_PBUFFER", surfaceLargestPbuffer, true, ""},
        {"EGL_MIPMAP_TEXTURE", surfaceMipmapTexture, surfaceTextureApplicable, "Requires EGL 1.1+"},
        {"EGL_MIPMAP_LEVEL", surfaceMipmapLevel, surfaceTextureApplicable, "Requires EGL 1.1+"},
        {"EGL_MULTISAMPLE_RESOLVE", surfaceMultisampleResolve, surfaceMultisampleApplicable, "Requires EGL 1.4+"},
        {"EGL_PIXEL_ASPECT_RATIO", surfacePixelAspectRatio, surfaceResolutionApplicable, "Requires EGL 1.2+"},
        {"EGL_RENDER_BUFFER/surface", surfaceRenderBuffer, surfaceResolutionApplicable, "Requires EGL 1.2+"},
        {"EGL_SWAP_BEHAVIOR", surfaceSwapBehavior, surfaceResolutionApplicable, "Requires EGL 1.2+"},
        {"EGL_TEXTURE_FORMAT", surfaceTextureFormat, surfaceTextureApplicable, "Requires EGL 1.1+"},
        {"EGL_TEXTURE_TARGET", surfaceTextureTarget, surfaceTextureApplicable, "Requires EGL 1.1+"},
        {"EGL_VERTICAL_RESOLUTION", surfaceVerticalResolution, surfaceResolutionApplicable, "Requires EGL 1.2+"}
    };
    for (const auto& entry : eglRuntimeAttributes) {
        const std::string status = !entry.applicable ? "Not applicable" : entry.result.available ? "Available" : "Unavailable";
        const std::string detail = !entry.applicable ? entry.notApplicableDetail : entry.result.available ? "" : eglErrorDisplay(entry.result.error);
        diagnostics.push_back({entry.name, status, detail});
    }
    diagnostics.push_back({"eglQueryAPI", boundApi != EGL_NONE ? "Available" : "Unavailable", boundApi != EGL_NONE ? "" : "eglQueryAPI returned EGL_NONE"});
    diagnostics.push_back({"EGL current bindings", currentContext && currentDisplay && currentDrawSurface && currentReadSurface ? "Available" : "Unavailable", currentContext && currentDisplay && currentDrawSurface && currentReadSurface ? "" : "The collector context/display/surface binding did not match the current EGL state"});

    std::vector<EglCapabilityNative> eglCapabilities;
    bool eglCapabilityOverflow = false;
    auto addEglCapability = [&](const std::string& name, const std::string& status, const std::string& value, const std::string& detail = std::string()) {
        auto existing = std::find_if(eglCapabilities.begin(), eglCapabilities.end(), [&](const EglCapabilityNative& item) { return item.name == name; });
        if (existing != eglCapabilities.end()) {
            existing->status = "Unavailable";
            existing->value.clear();
            existing->detail = "Duplicate EGL capability identity returned by the collector; evidence was rejected instead of being reported ambiguously";
            return;
        }
        if (eglCapabilities.size() >= kMaxEglCapabilityCount) {
            eglCapabilityOverflow = true;
            return;
        }
        eglCapabilities.push_back({name, status, value, detail});
    };
    addEglCapability(
        "EGL context creation request",
        contextCreation.requestedMajor > 0 ? "Available" : "Unavailable",
        contextCreation.requestedMajor > 0 ? ("OpenGL ES " + std::to_string(contextCreation.requestedMajor) + "." + std::to_string(contextCreation.requestedMinor)) : "",
        contextCreation.path.empty() ? "No context creation path completed" : contextCreation.path + "; actual runtime version is reported from OpenGL ES GL_VERSION/GL_MAJOR_VERSION/GL_MINOR_VERSION evidence"
    );
    addEglCapability("EGL_CONTEXT_MINOR_VERSION", "Not applicable", "", "Creation attribute, not legal EGL 1.5 eglQueryContext runtime state; the request is recorded separately and the actual client version comes from OpenGL ES runtime identity");
    addEglCapability("EGL_CONTEXT_OPENGL_RESET_NOTIFICATION_STRATEGY", "Not applicable", "", "Creation attribute; runtime robustness/reset strategy belongs to OpenGL ES state and is not queried through eglQueryContext");
    addEglCapability("EGL_CONTEXT_FLAGS_KHR", "Not applicable", "", "Creation attribute; OpenGL ES runtime context flags are reported through GL_CONTEXT_FLAGS when available");
    addEglCapability("EGL_CONTEXT_OPENGL_NO_ERROR_KHR", "Not applicable", "", hasExt(displayExt, "EGL_KHR_create_context_no_error") ? "Creation-only attribute with no EGL queryable state; collector does not request a no-error context" : "EGL_KHR_create_context_no_error is not advertised");
    if (hasExt(displayExt, "EGL_EXT_protected_content")) {
        const auto protectedSurface = querySurfaceAttr(d, s, EGL_PROTECTED_CONTENT_EXT_VALUE);
        const auto protectedContext = queryContextAttr(d, c, EGL_PROTECTED_CONTENT_EXT_VALUE);
        addEglCapability("EGL_PROTECTED_CONTENT_EXT/surface", protectedSurface.available ? "Available" : "Unavailable", protectedSurface.available ? (protectedSurface.value == EGL_TRUE ? "EGL_TRUE" : "EGL_FALSE") : "", protectedSurface.available ? "Collector pbuffer protected-content state" : eglErrorDisplay(protectedSurface.error));
        addEglCapability("EGL_PROTECTED_CONTENT_EXT/context", protectedContext.available ? "Available" : "Unavailable", protectedContext.available ? (protectedContext.value == EGL_TRUE ? "EGL_TRUE" : "EGL_FALSE") : "", protectedContext.available ? "Collector context protected-content state" : eglErrorDisplay(protectedContext.error));
    }
    if (hasExt(displayExt, "EGL_KHR_partial_update") || hasExt(displayExt, "EGL_EXT_buffer_age")) {
        addEglCapability("EGL_BUFFER_AGE_KHR", "Not applicable", "", "Window-surface presentation evidence; the isolated collector intentionally uses a pbuffer");
    }

    using QueryDisplayAttribExtProc = EGLBoolean (*)(EGLDisplay, EGLint, EGLAttrib*);
    using QueryDeviceStringExtProc = const char* (*)(EGLDeviceEXT, EGLint);
    using QueryDeviceAttribExtProc = EGLBoolean (*)(EGLDeviceEXT, EGLint, EGLAttrib*);
    using QueryDevicesExtProc = EGLBoolean (*)(EGLint, EGLDeviceEXT*, EGLint*);
    using GetDisplayDriverNameMesaProc = const char* (*)(EGLDisplay);
    using QueryDmaBufFormatsExtProc = EGLBoolean (*)(EGLDisplay, EGLint, EGLint*, EGLint*);
    using QueryDmaBufModifiersExtProc = EGLBoolean (*)(EGLDisplay, EGLint, EGLint, EGLuint64KHR*, EGLBoolean*, EGLint*);
    const auto queryDisplayAttribExt = reinterpret_cast<QueryDisplayAttribExtProc>(eglGetProcAddress("eglQueryDisplayAttribEXT"));
    const auto queryDeviceStringExt = reinterpret_cast<QueryDeviceStringExtProc>(eglGetProcAddress("eglQueryDeviceStringEXT"));
    const auto queryDeviceAttribExt = reinterpret_cast<QueryDeviceAttribExtProc>(eglGetProcAddress("eglQueryDeviceAttribEXT"));
    const auto queryDevicesExt = reinterpret_cast<QueryDevicesExtProc>(eglGetProcAddress("eglQueryDevicesEXT"));
    auto queryDeviceText = [&](EGLDeviceEXT device, EGLint token) -> std::pair<const char*, std::string> {
        if (!queryDeviceStringExt) return {nullptr, "eglQueryDeviceStringEXT was not resolved"};
        eglGetError();
        const char* raw = queryDeviceStringExt(device, token);
        const EGLint error = eglGetError();
        if (error != EGL_SUCCESS) return {nullptr, eglErrorDisplay(error)};
        if (!runtimeStringValid(raw)) return {nullptr, "eglQueryDeviceStringEXT returned null, malformed or oversized UTF-8 text"};
        return {raw, ""};
    };
    const bool hasDeviceQuery = hasExt(clientExt, "EGL_EXT_device_query") || hasExt(clientExt, "EGL_EXT_device_base");
    if (hasDeviceQuery && queryDisplayAttribExt && queryDeviceStringExt) {
        EGLAttrib rawDevice = 0;
        eglGetError();
        const EGLBoolean deviceRead = queryDisplayAttribExt(d, EGL_DEVICE_EXT_VALUE, &rawDevice);
        const EGLint deviceReadError = eglGetError();
        if (deviceRead == EGL_TRUE && deviceReadError == EGL_SUCCESS && rawDevice != 0) {
            const auto device = reinterpret_cast<EGLDeviceEXT>(rawDevice);
            const auto deviceExtText = queryDeviceText(device, EGL_EXTENSIONS);
            bool deviceExtComplete = false;
            const auto deviceExt = deviceExtText.first ? splitExt(deviceExtText.first, &deviceExtComplete) : std::vector<std::string>{};
            if (!deviceExtComplete) {
                addEglCapability("EGL_DEVICE_EXT", "Unavailable", "", deviceExtText.second.empty() ? "Device extension enumeration is malformed or exceeded a safety bound" : deviceExtText.second);
            } else {
            addEglCapability("EGL_DEVICE_EXT", "Available", "Current display device", "Device extensions: " + std::to_string(deviceExt.size()));
            if (hasExt(deviceExt, "EGL_EXT_device_query_name")) {
                const auto renderer = queryDeviceText(device, EGL_RENDERER_EXT_VALUE);
                addEglCapability("EGL_RENDERER_EXT", renderer.first ? "Available" : "Unavailable", renderer.first ? renderer.first : "", renderer.second);
            }
            if (hasExt(deviceExt, "EGL_EXT_device_persistent_id")) {
                const auto driverName = queryDeviceText(device, EGL_DRIVER_NAME_EXT_VALUE);
                addEglCapability("EGL_DRIVER_NAME_EXT", driverName.first ? "Available" : "Unavailable", driverName.first ? driverName.first : "", driverName.first ? "Persistent UUID fields are intentionally not collected" : driverName.second);
            }
            if (hasExt(deviceExt, "EGL_EXT_device_type") && queryDeviceAttribExt) {
                EGLAttrib deviceType = 0;
                eglGetError();
                const bool ok = queryDeviceAttribExt(device, EGL_DEVICE_TYPE_EXT_VALUE, &deviceType) == EGL_TRUE;
                const EGLint err = ok ? EGL_SUCCESS : eglGetError();
                addEglCapability("EGL_DEVICE_TYPE_EXT", ok ? "Available" : "Unavailable", ok ? eglDeviceTypeDisplay(deviceType) : "", ok ? "" : eglErrorDisplay(err));
            }
            }
        } else {
            addEglCapability("EGL_DEVICE_EXT", "Unavailable", "", deviceReadError == EGL_SUCCESS ? "eglQueryDisplayAttribEXT returned no valid EGLDevice handle" : eglErrorDisplay(deviceReadError));
        }
    } else if (hasDeviceQuery) {
        addEglCapability("EGL_EXT_device_query", "Unavailable", "", "Required EGL_EXT_device_query entry points were not resolved");
    }

    const bool hasDeviceEnumeration = hasExt(clientExt, "EGL_EXT_device_enumeration") || hasExt(clientExt, "EGL_EXT_device_base");
    if (hasDeviceEnumeration && queryDevicesExt) {
        EGLint count = 0;
        eglGetError();
        if (queryDevicesExt(0, nullptr, &count) == EGL_TRUE && count >= 0 && count <= kMaxEglDeviceCount) {
            std::vector<EGLDeviceEXT> devices(static_cast<size_t>(count));
            EGLint written = 0;
            const bool readOk = count == 0 || queryDevicesExt(count, devices.data(), &written) == EGL_TRUE;
            const bool ok = readOk && stableCount(count, written);
            addEglCapability("EGL_EXT_device_enumeration", ok ? "Available" : "Unavailable", ok ? std::to_string(written) + " device(s)" : "", ok ? "Bounded to 32 devices; no UUID or raw handle is reported" : readOk ? "Device count changed between two enumeration passes; incomplete evidence rejected" : eglErrorDisplay(eglGetError()));
            if (ok && queryDeviceStringExt) {
                for (EGLint i = 0; i < written; ++i) {
                    const auto device = devices[static_cast<size_t>(i)];
                    const auto extText = queryDeviceText(device, EGL_EXTENSIONS);
                    bool extComplete = false;
                    const auto extList = extText.first ? splitExt(extText.first, &extComplete) : std::vector<std::string>{};
                    const auto renderer = extComplete && hasExt(extList, "EGL_EXT_device_query_name") ? queryDeviceText(device, EGL_RENDERER_EXT_VALUE) : std::pair<const char*, std::string>{nullptr, ""};
                    addEglCapability("EGL enumerated device " + std::to_string(i), extComplete ? "Available" : "Unavailable", extComplete ? (renderer.first ? renderer.first : "") : "", extComplete ? "Device extensions: " + std::to_string(extList.size()) + "; enumeration index is session-local; persistent identifiers are not reported" + (renderer.second.empty() ? (hasExt(extList, "EGL_EXT_device_query_name") ? "" : "; renderer query not applicable: EGL_EXT_device_query_name absent") : "; renderer query unavailable: " + renderer.second) : extText.second.empty() ? "Device extension enumeration is malformed or exceeded a safety bound" : extText.second);
                }
            }
        } else {
            addEglCapability("EGL_EXT_device_enumeration", "Unavailable", "", count > kMaxEglDeviceCount ? "Driver reported more than the 32-device safety bound" : eglErrorDisplay(eglGetError()));
        }
    }

    if (hasExt(clientExt, "EGL_KHR_display_reference")) {
        using QueryDisplayAttribKhrProc = EGLBoolean (*)(EGLDisplay, EGLint, EGLAttrib*);
        const auto queryDisplayAttribKhr = reinterpret_cast<QueryDisplayAttribKhrProc>(eglGetProcAddress("eglQueryDisplayAttribKHR"));
        EGLAttrib trackReferences = 0;
        eglGetError();
        const bool ok = queryDisplayAttribKhr && queryDisplayAttribKhr(d, EGL_TRACK_REFERENCES_KHR_VALUE, &trackReferences) == EGL_TRUE;
        const EGLint err = ok ? EGL_SUCCESS : eglGetError();
        addEglCapability("EGL_TRACK_REFERENCES_KHR", ok ? "Available" : "Unavailable", ok ? (trackReferences == EGL_TRUE ? "EGL_TRUE" : "EGL_FALSE") : "", ok ? "Initialized EGLDisplay reference-counting behavior" : (queryDisplayAttribKhr ? eglErrorDisplay(err) : "eglQueryDisplayAttribKHR was not resolved"));
    }

    if (hasExt(displayExt, "EGL_MESA_query_driver")) {
        const auto getDriverName = reinterpret_cast<GetDisplayDriverNameMesaProc>(eglGetProcAddress("eglGetDisplayDriverName"));
        eglGetError();
        const char* driverName = getDriverName ? getDriverName(d) : nullptr;
        const EGLint driverNameError = getDriverName ? eglGetError() : EGL_SUCCESS;
        const bool driverNameValid = driverNameError == EGL_SUCCESS && runtimeStringValid(driverName);
        addEglCapability("EGL_MESA_query_driver/name", driverNameValid ? "Available" : "Unavailable", driverNameValid ? driverName : "", driverNameValid ? "Driver name from eglGetDisplayDriverName; driver-option XML is intentionally not retained because it is heap-allocated and potentially unbounded configuration text" : driverNameError != EGL_SUCCESS ? eglErrorDisplay(driverNameError) : getDriverName ? "eglGetDisplayDriverName returned null, malformed or oversized text" : "eglGetDisplayDriverName was not resolved");
    }

    if (hasExt(displayExt, "EGL_EXT_image_dma_buf_import_modifiers")) {
        const auto queryFormats = reinterpret_cast<QueryDmaBufFormatsExtProc>(eglGetProcAddress("eglQueryDmaBufFormatsEXT"));
        const auto queryModifiers = reinterpret_cast<QueryDmaBufModifiersExtProc>(eglGetProcAddress("eglQueryDmaBufModifiersEXT"));
        if (!queryFormats || !queryModifiers) {
            addEglCapability("EGL_EXT_image_dma_buf_import_modifiers", "Unavailable", "", "Extension advertised but required query entry points were not resolved");
        } else {
            EGLint formatCount = 0;
            eglGetError();
            const bool countOk = queryFormats(d, 0, nullptr, &formatCount) == EGL_TRUE;
            if (!countOk || formatCount < 0 || formatCount > kMaxDmaBufFormatCount) {
                addEglCapability("EGL_EXT_image_dma_buf_import_modifiers/formats", "Unavailable", "", formatCount > kMaxDmaBufFormatCount ? "Driver reported more than the 128-format safety bound" : countOk ? "Invalid negative format count" : eglErrorDisplay(eglGetError()));
            } else {
                std::vector<EGLint> formats(static_cast<size_t>(formatCount));
                EGLint writtenFormats = 0;
                const bool formatsRead = formatCount == 0 || queryFormats(d, formatCount, formats.data(), &writtenFormats) == EGL_TRUE;
                const bool formatsOk = formatsRead && stableCount(formatCount, writtenFormats);
                addEglCapability("EGL_EXT_image_dma_buf_import_modifiers/formats", formatsOk ? "Available" : "Unavailable", formatsOk ? std::to_string(writtenFormats) + " dma-buf format(s)" : "", formatsOk ? "Direct display capability query; format values are raw DRM FourCC codes" : formatsRead ? "Format count changed between enumeration passes; incomplete evidence rejected" : eglErrorDisplay(eglGetError()));
                size_t modifierTotal = 0;
                if (formatsOk) {
                    for (EGLint i = 0; i < writtenFormats; ++i) {
                        EGLint modifierCount = 0;
                        eglGetError();
                        const bool modifierCountOk = queryModifiers(d, formats[static_cast<size_t>(i)], 0, nullptr, nullptr, &modifierCount) == EGL_TRUE;
                        if (!modifierCountOk || modifierCount < 0 || modifierCount > kMaxDmaBufModifierCountPerFormat || modifierTotal + static_cast<size_t>(std::max(modifierCount, 0)) > kMaxDmaBufModifierCountTotal) {
                            addEglCapability("EGL dma-buf format " + hexv(formats[static_cast<size_t>(i)]), "Unavailable", "", modifierCount > kMaxDmaBufModifierCountPerFormat || modifierTotal + static_cast<size_t>(std::max(modifierCount, 0)) > kMaxDmaBufModifierCountTotal ? "Modifier result exceeded the per-format or total safety bound" : eglErrorDisplay(eglGetError()));
                            continue;
                        }
                        modifierTotal += static_cast<size_t>(modifierCount);
                        std::vector<EGLuint64KHR> modifiers(static_cast<size_t>(modifierCount));
                        std::vector<EGLBoolean> externalOnly(static_cast<size_t>(modifierCount));
                        EGLint writtenModifiers = 0;
                        const bool modifiersRead = modifierCount == 0 || queryModifiers(d, formats[static_cast<size_t>(i)], modifierCount, modifiers.data(), externalOnly.data(), &writtenModifiers) == EGL_TRUE;
                        const bool modifiersOk = modifiersRead && stableCount(modifierCount, writtenModifiers);
                        EGLint externalCount = 0;
                        std::string modifierDetail;
                        bool modifierFlagsValid = modifiersOk;
                        if (modifiersOk) {
                            for (EGLint j = 0; j < writtenModifiers; ++j) {
                                const auto index = static_cast<size_t>(j);
                                if (externalOnly[index] != EGL_TRUE && externalOnly[index] != EGL_FALSE) { modifierFlagsValid = false; break; }
                                if (externalOnly[index] == EGL_TRUE) ++externalCount;
                                if (!modifierDetail.empty()) modifierDetail += ", ";
                                modifierDetail += hexModifier(modifiers[index]);
                                modifierDetail += externalOnly[index] == EGL_TRUE ? "[externalOnly=true]" : "[externalOnly=false]";
                            }
                        }
                        addEglCapability("EGL dma-buf format " + hexv(formats[static_cast<size_t>(i)]), modifierFlagsValid ? "Available" : "Unavailable", modifierFlagsValid ? std::to_string(writtenModifiers) + " modifier(s)" : "", modifierFlagsValid ? std::to_string(externalCount) + " external-only modifier(s); raw DRM modifiers: " + modifierDetail : !modifiersOk ? (modifiersRead ? "Modifier count changed between enumeration passes; incomplete evidence rejected" : eglErrorDisplay(eglGetError())) : "Modifier returned an invalid externalOnly flag; incomplete evidence rejected");
                    }
                }
            }
        }
    }

    if (hasExt(displayExt, "EGL_EXT_surface_compression")) {
        using QueryCompressionProc = EGLBoolean (*)(EGLDisplay, EGLConfig, const EGLAttrib*, EGLint*, EGLint, EGLint*);
        const auto queryCompression = reinterpret_cast<QueryCompressionProc>(eglGetProcAddress("eglQuerySupportedCompressionRatesEXT"));
        if (queryCompression) {
            const EGLAttrib attrs[] = {EGL_NONE};
            EGLint count = 0;
            eglGetError();
            if (queryCompression(d, cfg, attrs, nullptr, 0, &count) == EGL_TRUE && count >= 0 && count <= kMaxEglCompressionRateCount) {
                std::vector<EGLint> rates(static_cast<size_t>(count));
                EGLint written = 0;
                const bool readOk = count == 0 || queryCompression(d, cfg, attrs, rates.data(), count, &written) == EGL_TRUE;
                const bool ok = readOk && stableCount(count, written);
                std::string value;
                if (ok) for (EGLint i = 0; i < written; ++i) { if (!value.empty()) value += ", "; value += eglCompressionRateDisplay(rates[static_cast<size_t>(i)]); }
                addEglCapability("EGL_EXT_surface_compression rates", ok ? "Available" : "Unavailable", ok ? (value.empty() ? "No fixed-rate compression rates" : value) : "", ok ? "eglQuerySupportedCompressionRatesEXT for the collector EGLConfig" : readOk ? "Compression-rate count changed between enumeration passes; incomplete evidence rejected" : eglErrorDisplay(eglGetError()));
            } else {
                addEglCapability("EGL_EXT_surface_compression rates", "Unavailable", "", count > kMaxEglCompressionRateCount ? "Driver reported more than the 32-rate safety bound" : eglErrorDisplay(eglGetError()));
            }
        } else {
            addEglCapability("EGL_EXT_surface_compression rates", "Unavailable", "", "eglQuerySupportedCompressionRatesEXT was not resolved");
        }
    }

    if (eglCapabilityOverflow) {
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL capability evidence exceeded the 256-record safety bound\"}");
    }

    std::ostringstream o;
    o << "{\"status\":\"available\",\"renderer\":" << q(glRenderer)
      << ",\"vendor\":" << q(glVendor)
      << ",\"glVersion\":" << q(glVersion)
      << ",\"glMajor\":" << parsed.first
      << ",\"glMinor\":" << parsed.second
      << ",\"glslVersion\":" << q(glslVersion)
      << ",\"egl\":{\"vendor\":" << q(eglVendorText ? eglVendorText : "Unavailable")
      << ",\"version\":" << q(eglVersionText ? eglVersionText : "Unavailable")
      << ",\"initializedVersion\":" << q(std::to_string(eglMajor) + "." + std::to_string(eglMinor))
      << ",\"clientApis\":" << q(eglClientApisText ? eglClientApisText : "Unavailable")
      << ",\"extensions\":";
    appendStringArray(o, displayExt);
    o << ",\"clientExtensions\":";
    appendStringArray(o, clientExt);
    o << "},\"glRuntime\":{\"contextFlags\":" << (glContextFlags.available ? q(glContextFlags.value) : "null")
      << ",\"resetNotificationStrategy\":" << (glResetStrategy.available ? q(glResetStrategy.value) : "null")
      << ",\"resetNotificationStrategyQuery\":" << (glResetStrategy.applicable ? q(glResetStrategy.name) : "null")
      << ",\"robustAccess\":" << (glRobustAccess.available ? (glRobustAccess.value == "True" ? "true" : "false") : "null")
      << ",\"robustAccessQuery\":" << (glRobustAccess.applicable ? q(glRobustAccessQuery) : "null")
      << ",\"unavailableAttributes\":[";
    bool glRuntimeFailureFirst = true;
    for (const auto* item : {&glContextFlags, &glResetStrategy, &glRobustAccess}) {
        if (!item->applicable || item->available) continue;
        if (!glRuntimeFailureFirst) o << ',';
        glRuntimeFailureFirst = false;
        o << "{\"name\":" << q(item->name) << ",\"error\":" << q(item->detail) << '}';
    }
    o << "]},\"eglRuntime\":{\"boundApi\":" << q(eglApiDisplay(boundApi))
      << ",\"configId\":" << (contextConfigId.available ? std::to_string(contextConfigId.value) : "null")
      << ",\"clientType\":" << (contextClientType.available ? q(eglEnumDisplay(contextClientType.value)) : "null")
      << ",\"clientVersion\":" << (contextClientVersion.available ? std::to_string(contextClientVersion.value) : "null")
      << ",\"renderBuffer\":" << (contextRenderBuffer.available ? q(eglEnumDisplay(contextRenderBuffer.value)) : "null")
      << ",\"currentContext\":" << (currentContext ? "true" : "false")
      << ",\"currentDisplay\":" << (currentDisplay ? "true" : "false")
      << ",\"currentDrawSurface\":" << (currentDrawSurface ? "true" : "false")
      << ",\"currentReadSurface\":" << (currentReadSurface ? "true" : "false")
      << ",\"surfaceGlColorspace\":" << (surfaceGlColorspace.available ? q(eglGlColorspaceDisplay(surfaceGlColorspace.value)) : "null")
      << ",\"surfaceGlColorspaceQuery\":" << (surfaceGlColorspaceQuery ? q(surfaceGlColorspaceQuery) : "null")
      << ",\"surfaceVgAlphaFormat\":" << (surfaceVgAlphaFormat.available ? q(eglVgAlphaFormatDisplay(surfaceVgAlphaFormat.value)) : "null")
      << ",\"surfaceVgAlphaFormatQuery\":" << (surfaceVgAlphaFormatQuery ? q(surfaceVgAlphaFormatQuery) : "null")
      << ",\"surfaceVgColorspace\":" << (surfaceVgColorspace.available ? q(eglVgColorspaceDisplay(surfaceVgColorspace.value)) : "null")
      << ",\"surfaceVgColorspaceQuery\":" << (surfaceVgColorspaceQuery ? q(surfaceVgColorspaceQuery) : "null")
      << ",\"surfaceConfigId\":" << (surfaceConfigId.available ? std::to_string(surfaceConfigId.value) : "null")
      << ",\"surfaceWidth\":" << (surfaceWidth.available ? std::to_string(surfaceWidth.value) : "null")
      << ",\"surfaceHeight\":" << (surfaceHeight.available ? std::to_string(surfaceHeight.value) : "null")
      << ",\"surfaceHorizontalResolution\":" << (surfaceHorizontalResolution.available ? std::to_string(surfaceHorizontalResolution.value) : "null")
      << ",\"surfaceLargestPbuffer\":" << (surfaceLargestPbuffer.available ? (surfaceLargestPbuffer.value == EGL_TRUE ? "true" : "false") : "null")
      << ",\"surfacePixelAspectRatio\":" << (surfacePixelAspectRatio.available ? std::to_string(surfacePixelAspectRatio.value) : "null")
      << ",\"surfaceVerticalResolution\":" << (surfaceVerticalResolution.available ? std::to_string(surfaceVerticalResolution.value) : "null")
      << ",\"surfaceRenderBuffer\":" << (surfaceRenderBuffer.available ? q(eglEnumDisplay(surfaceRenderBuffer.value)) : "null")
      << ",\"surfaceSwapBehavior\":" << (surfaceSwapBehavior.available ? q(eglEnumDisplay(surfaceSwapBehavior.value)) : "null")
      << ",\"surfaceTextureFormat\":" << (surfaceTextureFormat.available ? q(eglEnumDisplay(surfaceTextureFormat.value)) : "null")
      << ",\"surfaceTextureTarget\":" << (surfaceTextureTarget.available ? q(eglEnumDisplay(surfaceTextureTarget.value)) : "null")
      << ",\"surfaceMipmapTexture\":" << (surfaceMipmapTexture.available ? (surfaceMipmapTexture.value == EGL_TRUE ? "true" : "false") : "null")
      << ",\"surfaceMipmapLevel\":" << (surfaceMipmapLevel.available ? std::to_string(surfaceMipmapLevel.value) : "null")
      << ",\"surfaceMultisampleResolve\":" << (surfaceMultisampleResolve.available ? q(eglEnumDisplay(surfaceMultisampleResolve.value)) : "null")
      << ",\"unavailableAttributes\":[";
    bool eglRuntimeFailureFirst = true;
    for (const auto& entry : eglRuntimeAttributes) {
        if (!entry.applicable || entry.result.available) continue;
        if (!eglRuntimeFailureFirst) o << ',';
        eglRuntimeFailureFirst = false;
        o << "{\"name\":" << q(entry.name) << ",\"error\":" << q(eglErrorDisplay(entry.result.error)) << '}';
    }
    o << "]},\"eglCapabilities\":[";
    for (size_t i = 0; i < eglCapabilities.size(); ++i) {
        if (i) o << ',';
        const auto& cap = eglCapabilities[i];
        o << "{\"name\":" << q(cap.name) << ",\"status\":" << q(cap.status) << ",\"value\":" << q(cap.value) << ",\"detail\":" << q(cap.detail) << "}";
    }
    o << "],\"extensions\":";
    appendStringArray(o, glExt);

    o << ",\"limits\":[";
    bool first = true;
    addLimit(o, first, "GL_MAX_TEXTURE_SIZE", GL_MAX_TEXTURE_SIZE);
    addLimit(o, first, "GL_NUM_COMPRESSED_TEXTURE_FORMATS", GL_NUM_COMPRESSED_TEXTURE_FORMATS);
    addLimit(o, first, "GL_NUM_SHADER_BINARY_FORMATS", GL_NUM_SHADER_BINARY_FORMATS);
    addLimit(o, first, "GL_MAX_CUBE_MAP_TEXTURE_SIZE", GL_MAX_CUBE_MAP_TEXTURE_SIZE);
    addLimit(o, first, "GL_MAX_RENDERBUFFER_SIZE", GL_MAX_RENDERBUFFER_SIZE);
    addLimit2(o, first, "GL_MAX_VIEWPORT_DIMS", GL_MAX_VIEWPORT_DIMS);
    addFloatLimit2(o, first, "GL_ALIASED_LINE_WIDTH_RANGE", GL_ALIASED_LINE_WIDTH_RANGE);
    addFloatLimit2(o, first, "GL_ALIASED_POINT_SIZE_RANGE", GL_ALIASED_POINT_SIZE_RANGE);
    addLimit(o, first, "GL_SUBPIXEL_BITS", GL_SUBPIXEL_BITS);
    addLimit(o, first, "GL_RED_BITS", GL_RED_BITS);
    addLimit(o, first, "GL_GREEN_BITS", GL_GREEN_BITS);
    addLimit(o, first, "GL_BLUE_BITS", GL_BLUE_BITS);
    addLimit(o, first, "GL_ALPHA_BITS", GL_ALPHA_BITS);
    addLimit(o, first, "GL_DEPTH_BITS", GL_DEPTH_BITS);
    addLimit(o, first, "GL_STENCIL_BITS", GL_STENCIL_BITS);
    addBooleanLimit(o, first, "GL_SHADER_COMPILER", GL_SHADER_COMPILER);
    addHexLimit(o, first, "GL_IMPLEMENTATION_COLOR_READ_FORMAT", GL_IMPLEMENTATION_COLOR_READ_FORMAT);
    addHexLimit(o, first, "GL_IMPLEMENTATION_COLOR_READ_TYPE", GL_IMPLEMENTATION_COLOR_READ_TYPE);
    if (hasExt(glExt, "GL_EXT_texture_filter_anisotropic")) addFloatLimit(o, first, "GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT", 0x84FF);
    if (glCode < 320 && hasExt(glExt, "GL_KHR_debug")) {
        addLimit(o, first, "GL_MAX_DEBUG_MESSAGE_LENGTH", 0x9143);
        addLimit(o, first, "GL_MAX_DEBUG_LOGGED_MESSAGES", 0x9144);
        addLimit(o, first, "GL_MAX_DEBUG_GROUP_STACK_DEPTH", 0x826C);
        addLimit(o, first, "GL_MAX_LABEL_LENGTH", 0x82E8);
    }
    if (hasExt(glExt, "GL_EXT_disjoint_timer_query")) {
        const auto getQueryivExt = reinterpret_cast<GetQueryivExtProc>(eglGetProcAddress("glGetQueryivEXT"));
        addQueryCounterBitsExt(o, first, getQueryivExt, "Query counter bits: GL_TIME_ELAPSED_EXT", 0x88BF);
        addQueryCounterBitsExt(o, first, getQueryivExt, "Query counter bits: GL_TIMESTAMP_EXT", 0x8E28);
    }
    if (hasExt(glExt, "GL_EXT_blend_func_extended")) addLimit(o, first, "GL_MAX_DUAL_SOURCE_DRAW_BUFFERS_EXT", 0x88FC);
    if (hasExt(glExt, "GL_OVR_multiview") || hasExt(glExt, "GL_OVR_multiview2")) addLimit(o, first, "GL_MAX_VIEWS_OVR", 0x9631);
    if (hasExt(glExt, "GL_EXT_multiview_draw_buffers")) addLimit(o, first, "GL_MAX_MULTIVIEW_BUFFERS_EXT", 0x90F2);
    if (hasExt(glExt, "GL_EXT_texture_buffer")) {
        addLimit(o, first, "GL_MAX_TEXTURE_BUFFER_SIZE_EXT", 0x8C2B);
        addLimit(o, first, "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT_EXT", 0x919F);
    }
    if (hasExt(glExt, "GL_EXT_clip_cull_distance")) {
        addLimit(o, first, "GL_MAX_CLIP_DISTANCES_EXT", 0x0D32);
        addLimit(o, first, "GL_MAX_CULL_DISTANCES_EXT", 0x82F9);
        addLimit(o, first, "GL_MAX_COMBINED_CLIP_AND_CULL_DISTANCES_EXT", 0x82FA);
    }
    if (glCode < 300 && hasExt(glExt, "GL_EXT_draw_buffers")) {
        addLimit(o, first, "GL_MAX_DRAW_BUFFERS_EXT", 0x8824);
        addLimit(o, first, "GL_MAX_COLOR_ATTACHMENTS_EXT", 0x8CDF);
    }
    if (glCode < 300 && hasExt(glExt, "GL_NV_draw_buffers")) addLimit(o, first, "GL_MAX_DRAW_BUFFERS_NV", 0x8824);
    if (glCode < 300 && hasExt(glExt, "GL_EXT_multisampled_render_to_texture")) addLimit(o, first, "GL_MAX_SAMPLES_EXT", 0x8D57);
    if (glCode < 300 && hasExt(glExt, "GL_NV_framebuffer_multisample")) addLimit(o, first, "GL_MAX_SAMPLES_NV", 0x8D57);
    if (glCode < 300 && hasExt(glExt, "GL_IMG_multisampled_render_to_texture")) addLimit(o, first, "GL_MAX_SAMPLES_IMG", 0x9135);
    if (glCode < 300 && hasExt(glExt, "GL_OES_get_program_binary")) addLimit(o, first, "GL_NUM_PROGRAM_BINARY_FORMATS", 0x87FE);
    if (hasExt(glExt, "GL_KHR_shader_subgroup")) {
        addLimit(o, first, "GL_SUBGROUP_SIZE_KHR", 0x9532);
        addHexLimit(o, first, "GL_SUBGROUP_SUPPORTED_STAGES_KHR", 0x9533);
        addHexLimit(o, first, "GL_SUBGROUP_SUPPORTED_FEATURES_KHR", 0x9534);
        addBooleanLimit(o, first, "GL_SUBGROUP_QUAD_ALL_STAGES_KHR", 0x9535);
    }
    if (hasExt(glExt, "GL_EXT_window_rectangles")) {
        addLimit(o, first, "GL_MAX_WINDOW_RECTANGLES_EXT", 0x8F14);
    }
    if (hasExt(glExt, "GL_OES_viewport_array")) {
        addLimit(o, first, "GL_MAX_VIEWPORTS_OES", 0x825B);
        addLimit(o, first, "GL_VIEWPORT_SUBPIXEL_BITS_OES", 0x825C);
        addFloatLimit2(o, first, "GL_VIEWPORT_BOUNDS_RANGE_OES", 0x825D);
        addHexLimit(o, first, "GL_VIEWPORT_INDEX_PROVOKING_VERTEX_OES", 0x825F);
    }
    if (hasExt(glExt, "GL_EXT_shader_pixel_local_storage")) {
        addLimit(o, first, "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_FAST_SIZE_EXT", 0x8F63);
        addLimit(o, first, "GL_MAX_SHADER_PIXEL_LOCAL_STORAGE_SIZE_EXT", 0x8F67);
    }
    if (hasExt(glExt, "GL_EXT_shader_pixel_local_storage2")) {
        addLimit(o, first, "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_FAST_SIZE_EXT", 0x9650);
        addLimit(o, first, "GL_MAX_SHADER_COMBINED_LOCAL_STORAGE_SIZE_EXT", 0x9651);
    }
    if (glCode < 320 && hasExt(glExt, "GL_OES_sample_shading")) addFloatLimit(o, first, "GL_MIN_SAMPLE_SHADING_VALUE_OES", 0x8C37);
    if (hasExt(glExt, "GL_EXT_sparse_texture")) {
        addLimit(o, first, "GL_MAX_SPARSE_TEXTURE_SIZE_EXT", 0x9198);
        addLimit(o, first, "GL_MAX_SPARSE_3D_TEXTURE_SIZE_EXT", 0x9199);
        addLimit(o, first, "GL_MAX_SPARSE_ARRAY_TEXTURE_LAYERS_EXT", 0x919A);
        addBooleanLimit(o, first, "GL_SPARSE_TEXTURE_FULL_ARRAY_CUBE_MIPMAPS_EXT", 0x91A9);
    }
    if (hasExt(glExt, "GL_EXT_fragment_shading_rate")) {
        addBooleanLimit(o, first, "GL_FRAGMENT_SHADING_RATE_WITH_SHADER_DEPTH_STENCIL_WRITES_SUPPORTED_EXT", 0x96DD);
        addBooleanLimit(o, first, "GL_FRAGMENT_SHADING_RATE_WITH_SAMPLE_MASK_SUPPORTED_EXT", 0x96DE);
        addBooleanLimit(o, first, "GL_FRAGMENT_SHADING_RATE_NON_TRIVIAL_COMBINERS_SUPPORTED_EXT", 0x8F6F);
        const auto getFragmentShadingRatesExt = reinterpret_cast<GetFragmentShadingRatesExtProc>(eglGetProcAddress("glGetFragmentShadingRatesEXT"));
        addFragmentShadingRates(o, first, getFragmentShadingRatesExt, 1);
        addFragmentShadingRates(o, first, getFragmentShadingRatesExt, 4);
        addLimit(o, first, "GL_MIN_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_WIDTH_EXT", 0x96D7);
        addLimit(o, first, "GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_WIDTH_EXT", 0x96D8);
        addLimit(o, first, "GL_MIN_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_HEIGHT_EXT", 0x96D9);
        addLimit(o, first, "GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_HEIGHT_EXT", 0x96DA);
        addLimit(o, first, "GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_TEXEL_ASPECT_RATIO_EXT", 0x96DB);
        addLimit(o, first, "GL_MAX_FRAGMENT_SHADING_RATE_ATTACHMENT_LAYERS_EXT", 0x96DC);
        addBooleanLimit(o, first, "GL_FRAGMENT_SHADING_RATE_ATTACHMENT_WITH_DEFAULT_FRAMEBUFFER_SUPPORTED_EXT", 0x96DF);
    }
    if (hasExt(glExt, "GL_EXT_memory_object") || hasExt(glExt, "GL_EXT_semaphore")) {
        addLimit(o, first, "GL_NUM_DEVICE_UUIDS_EXT", 0x9596);
    }
    if (hasExt(glExt, "GL_NV_timeline_semaphore")) {
        addLimit64(o, first, "GL_MAX_TIMELINE_SEMAPHORE_VALUE_DIFFERENCE_NV", 0x95B6);
    }
    if (hasExt(glExt, "GL_NV_fbo_color_attachments")) {
        addLimit(o, first, "GL_MAX_COLOR_ATTACHMENTS_NV", 0x8CDF);
    }
    if (hasExt(glExt, "GL_ARM_shader_core_properties")) {
        addLimit(o, first, "GL_SHADER_CORE_COUNT_ARM", 0x96F0);
        addLimit(o, first, "GL_SHADER_CORE_ACTIVE_COUNT_ARM", 0x96F1);
        addHexLimit64(o, first, "GL_SHADER_CORE_PRESENT_MASK_ARM", 0x96F2);
        addLimit(o, first, "GL_SHADER_CORE_MAX_WARP_COUNT_ARM", 0x96F3);
        addLimit(o, first, "GL_SHADER_CORE_PIXEL_RATE_ARM", 0x96F4);
        addLimit(o, first, "GL_SHADER_CORE_TEXEL_RATE_ARM", 0x96F5);
        addLimit(o, first, "GL_SHADER_CORE_FMA_RATE_ARM", 0x96F6);
    }
    if (hasExt(glExt, "GL_QCOM_motion_estimation")) {
        addLimit(o, first, "GL_MOTION_ESTIMATION_SEARCH_BLOCK_X_QCOM", GL_MOTION_ESTIMATION_SEARCH_BLOCK_X_QCOM_VALUE);
        addLimit(o, first, "GL_MOTION_ESTIMATION_SEARCH_BLOCK_Y_QCOM", GL_MOTION_ESTIMATION_SEARCH_BLOCK_Y_QCOM_VALUE);
    }
    if (hasExt(glExt, "GL_NV_shading_rate_image")) {
        addLimit(o, first, "GL_SHADING_RATE_IMAGE_TEXEL_WIDTH_NV", GL_SHADING_RATE_IMAGE_TEXEL_WIDTH_NV_VALUE);
        addLimit(o, first, "GL_SHADING_RATE_IMAGE_TEXEL_HEIGHT_NV", GL_SHADING_RATE_IMAGE_TEXEL_HEIGHT_NV_VALUE);
        addLimit(o, first, "GL_SHADING_RATE_IMAGE_PALETTE_SIZE_NV", GL_SHADING_RATE_IMAGE_PALETTE_SIZE_NV_VALUE);
        addLimit(o, first, "GL_MAX_COARSE_FRAGMENT_SAMPLES_NV", GL_MAX_COARSE_FRAGMENT_SAMPLES_NV_VALUE);
    }
    if (hasExt(glExt, "GL_NV_primitive_shading_rate")) {
        addLimit(o, first, "GL_SHADING_RATE_IMAGE_PALETTE_COUNT_NV", GL_SHADING_RATE_IMAGE_PALETTE_COUNT_NV_VALUE);
    }
    if (hasExt(glExt, "GL_ARM_shader_framebuffer_fetch")) {
        addBooleanLimit(o, first, "GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM", GL_FRAGMENT_SHADER_FRAMEBUFFER_FETCH_MRT_ARM_VALUE);
    }
    if (hasExt(glExt, "GL_EXT_mesh_shader")) {
        addLimit(o, first, "GL_MAX_TASK_UNIFORM_BLOCKS_EXT", 0x8E68);
        addLimit(o, first, "GL_MAX_TASK_TEXTURE_IMAGE_UNITS_EXT", 0x8E69);
        addLimit(o, first, "GL_MAX_TASK_IMAGE_UNIFORMS_EXT", 0x8E6A);
        addLimit(o, first, "GL_MAX_TASK_UNIFORM_COMPONENTS_EXT", 0x8E6B);
        addLimit(o, first, "GL_MAX_TASK_ATOMIC_COUNTER_BUFFERS_EXT", 0x8E6C);
        addLimit(o, first, "GL_MAX_TASK_ATOMIC_COUNTERS_EXT", 0x8E6D);
        addLimit(o, first, "GL_MAX_TASK_SHADER_STORAGE_BLOCKS_EXT", 0x8E6E);
        addLimit(o, first, "GL_MAX_COMBINED_TASK_UNIFORM_COMPONENTS_EXT", 0x8E6F);
        addLimit(o, first, "GL_MAX_MESH_UNIFORM_BLOCKS_EXT", 0x8E60);
        addLimit(o, first, "GL_MAX_MESH_TEXTURE_IMAGE_UNITS_EXT", 0x8E61);
        addLimit(o, first, "GL_MAX_MESH_IMAGE_UNIFORMS_EXT", 0x8E62);
        addLimit(o, first, "GL_MAX_MESH_UNIFORM_COMPONENTS_EXT", 0x8E63);
        addLimit(o, first, "GL_MAX_MESH_ATOMIC_COUNTER_BUFFERS_EXT", 0x8E64);
        addLimit(o, first, "GL_MAX_MESH_ATOMIC_COUNTERS_EXT", 0x8E65);
        addLimit(o, first, "GL_MAX_MESH_SHADER_STORAGE_BLOCKS_EXT", 0x8E66);
        addLimit(o, first, "GL_MAX_COMBINED_MESH_UNIFORM_COMPONENTS_EXT", 0x8E67);
        addLimit(o, first, "GL_MAX_TASK_WORK_GROUP_TOTAL_COUNT_EXT", 0x9740);
        addLimit(o, first, "GL_MAX_MESH_WORK_GROUP_TOTAL_COUNT_EXT", 0x9741);
        addLimit(o, first, "GL_MAX_MESH_WORK_GROUP_INVOCATIONS_EXT", 0x9757);
        addLimit(o, first, "GL_MAX_TASK_WORK_GROUP_INVOCATIONS_EXT", 0x9759);
        addLimit(o, first, "GL_MAX_TASK_PAYLOAD_SIZE_EXT", 0x9742);
        addLimit(o, first, "GL_MAX_TASK_SHARED_MEMORY_SIZE_EXT", 0x9743);
        addLimit(o, first, "GL_MAX_MESH_SHARED_MEMORY_SIZE_EXT", 0x9744);
        addLimit(o, first, "GL_MAX_TASK_PAYLOAD_AND_SHARED_MEMORY_SIZE_EXT", 0x9745);
        addLimit(o, first, "GL_MAX_MESH_PAYLOAD_AND_SHARED_MEMORY_SIZE_EXT", 0x9746);
        addLimit(o, first, "GL_MAX_MESH_OUTPUT_MEMORY_SIZE_EXT", 0x9747);
        addLimit(o, first, "GL_MAX_MESH_PAYLOAD_AND_OUTPUT_MEMORY_SIZE_EXT", 0x9748);
        addLimit(o, first, "GL_MAX_MESH_OUTPUT_VERTICES_EXT", 0x9538);
        addLimit(o, first, "GL_MAX_MESH_OUTPUT_PRIMITIVES_EXT", 0x9756);
        addLimit(o, first, "GL_MAX_MESH_OUTPUT_COMPONENTS_EXT", 0x9749);
        addLimit(o, first, "GL_MAX_MESH_OUTPUT_LAYERS_EXT", 0x974A);
        addLimit(o, first, "GL_MAX_MESH_MULTIVIEW_VIEW_COUNT_EXT", 0x9557);
        addLimit(o, first, "GL_MESH_OUTPUT_PER_VERTEX_GRANULARITY_EXT", 0x92DF);
        addLimit(o, first, "GL_MESH_OUTPUT_PER_PRIMITIVE_GRANULARITY_EXT", 0x9543);
        addLimit(o, first, "GL_MAX_PREFERRED_TASK_WORK_GROUP_INVOCATIONS_EXT", 0x974B);
        addLimit(o, first, "GL_MAX_PREFERRED_MESH_WORK_GROUP_INVOCATIONS_EXT", 0x974C);
        addBooleanLimit(o, first, "GL_MESH_PREFERS_LOCAL_INVOCATION_VERTEX_OUTPUT_EXT", 0x974D);
        addBooleanLimit(o, first, "GL_MESH_PREFERS_LOCAL_INVOCATION_PRIMITIVE_OUTPUT_EXT", 0x974E);
        addBooleanLimit(o, first, "GL_MESH_PREFERS_COMPACT_VERTEX_OUTPUT_EXT", 0x974F);
        addBooleanLimit(o, first, "GL_MESH_PREFERS_COMPACT_PRIMITIVE_OUTPUT_EXT", 0x9750);
        for (GLuint i = 0; i < 3; ++i) {
            addIndexedLimit(o, first, "GL_MAX_TASK_WORK_GROUP_COUNT_EXT", 0x9751, i);
            addIndexedLimit(o, first, "GL_MAX_MESH_WORK_GROUP_COUNT_EXT", 0x9752, i);
            addIndexedLimit(o, first, "GL_MAX_TASK_WORK_GROUP_SIZE_EXT", 0x975A, i);
            addIndexedLimit(o, first, "GL_MAX_MESH_WORK_GROUP_SIZE_EXT", 0x9758, i);
        }
    }
    addLimit(o, first, "GL_MAX_VERTEX_ATTRIBS", GL_MAX_VERTEX_ATTRIBS);
    addLimit(o, first, "GL_MAX_VERTEX_UNIFORM_VECTORS", GL_MAX_VERTEX_UNIFORM_VECTORS);
    addLimit(o, first, "GL_MAX_FRAGMENT_UNIFORM_VECTORS", GL_MAX_FRAGMENT_UNIFORM_VECTORS);
    addLimit(o, first, "GL_MAX_VARYING_VECTORS", GL_MAX_VARYING_VECTORS);
    addLimit(o, first, "GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS", GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
    addLimit(o, first, "GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS", GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS);
    addLimit(o, first, "GL_MAX_TEXTURE_IMAGE_UNITS", GL_MAX_TEXTURE_IMAGE_UNITS);

    if (glCode >= 320) {
    } else if (hasExt(glExt, "GL_KHR_robustness")) {
    }
    if (hasExt(glExt, "GL_KHR_robustness")) {
    }

    if (glCode >= 300) {
        addLimit(o, first, "GL_NUM_EXTENSIONS", GL_NUM_EXTENSIONS);
        addLimit(o, first, "GL_MAX_3D_TEXTURE_SIZE", GL_MAX_3D_TEXTURE_SIZE);
        addLimit(o, first, "GL_NUM_PROGRAM_BINARY_FORMATS", GL_NUM_PROGRAM_BINARY_FORMATS);
        addLimit64(o, first, "GL_MAX_ELEMENT_INDEX", GL_MAX_ELEMENT_INDEX);
        addLimit64(o, first, "GL_MAX_SERVER_WAIT_TIMEOUT", GL_MAX_SERVER_WAIT_TIMEOUT);
        addLimit64(o, first, "GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS", GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS);
        addLimit64(o, first, "GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS", GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_ARRAY_TEXTURE_LAYERS", GL_MAX_ARRAY_TEXTURE_LAYERS);
        addLimit(o, first, "GL_MAX_COLOR_ATTACHMENTS", GL_MAX_COLOR_ATTACHMENTS);
        addLimit(o, first, "GL_MAX_COMBINED_UNIFORM_BLOCKS", GL_MAX_COMBINED_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_DRAW_BUFFERS", GL_MAX_DRAW_BUFFERS);
        addLimit(o, first, "GL_MAX_ELEMENTS_INDICES", GL_MAX_ELEMENTS_INDICES);
        addLimit(o, first, "GL_MAX_ELEMENTS_VERTICES", GL_MAX_ELEMENTS_VERTICES);
        addLimit(o, first, "GL_MAX_FRAGMENT_INPUT_COMPONENTS", GL_MAX_FRAGMENT_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_FRAGMENT_UNIFORM_BLOCKS", GL_MAX_FRAGMENT_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_FRAGMENT_UNIFORM_COMPONENTS", GL_MAX_FRAGMENT_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_PROGRAM_TEXEL_OFFSET", GL_MAX_PROGRAM_TEXEL_OFFSET);
        addLimit(o, first, "GL_MIN_PROGRAM_TEXEL_OFFSET", GL_MIN_PROGRAM_TEXEL_OFFSET);
        addLimit(o, first, "GL_MAX_SAMPLES", GL_MAX_SAMPLES);
        addFloatLimit(o, first, "GL_MAX_TEXTURE_LOD_BIAS", GL_MAX_TEXTURE_LOD_BIAS);
        addLimit(o, first, "GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS", GL_MAX_TRANSFORM_FEEDBACK_INTERLEAVED_COMPONENTS);
        addLimit(o, first, "GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS", GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_ATTRIBS);
        addLimit(o, first, "GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS", GL_MAX_TRANSFORM_FEEDBACK_SEPARATE_COMPONENTS);
        addLimit64(o, first, "GL_MAX_UNIFORM_BLOCK_SIZE", GL_MAX_UNIFORM_BLOCK_SIZE);
        addLimit(o, first, "GL_MAX_UNIFORM_BUFFER_BINDINGS", GL_MAX_UNIFORM_BUFFER_BINDINGS);
        addLimit(o, first, "GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT", GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT);
        addLimit(o, first, "GL_MAX_VARYING_COMPONENTS", GL_MAX_VARYING_COMPONENTS);
        addLimit(o, first, "GL_MAX_VERTEX_OUTPUT_COMPONENTS", GL_MAX_VERTEX_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_VERTEX_UNIFORM_BLOCKS", GL_MAX_VERTEX_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_VERTEX_UNIFORM_COMPONENTS", GL_MAX_VERTEX_UNIFORM_COMPONENTS);
    }

    if (glCode >= 310) {
        addLimit(o, first, "GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS", GL_MAX_ATOMIC_COUNTER_BUFFER_BINDINGS);
        addLimit(o, first, "GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE", GL_MAX_ATOMIC_COUNTER_BUFFER_SIZE);
        addLimit(o, first, "GL_MAX_COLOR_TEXTURE_SAMPLES", GL_MAX_COLOR_TEXTURE_SAMPLES);
        addLimit(o, first, "GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES", GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES);
        addLimit(o, first, "GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS", GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS);
        addLimit(o, first, "GL_MAX_DEPTH_TEXTURE_SAMPLES", GL_MAX_DEPTH_TEXTURE_SAMPLES);
        addLimit(o, first, "GL_MAX_FRAGMENT_ATOMIC_COUNTERS", GL_MAX_FRAGMENT_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS", GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_FRAGMENT_IMAGE_UNIFORMS", GL_MAX_FRAGMENT_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS", GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_INTEGER_SAMPLES", GL_MAX_INTEGER_SAMPLES);
        addLimit(o, first, "GL_MAX_VERTEX_ATOMIC_COUNTERS", GL_MAX_VERTEX_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS", GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_VERTEX_IMAGE_UNIFORMS", GL_MAX_VERTEX_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS", GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_COMBINED_ATOMIC_COUNTERS", GL_MAX_COMBINED_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS", GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS", GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_COMBINED_IMAGE_UNIFORMS", GL_MAX_COMBINED_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS", GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_COMPUTE_ATOMIC_COUNTERS", GL_MAX_COMPUTE_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS", GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_COMPUTE_IMAGE_UNIFORMS", GL_MAX_COMPUTE_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS", GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_COMPUTE_SHARED_MEMORY_SIZE", GL_MAX_COMPUTE_SHARED_MEMORY_SIZE);
        addLimit(o, first, "GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS", GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_COMPUTE_UNIFORM_BLOCKS", GL_MAX_COMPUTE_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_COMPUTE_UNIFORM_COMPONENTS", GL_MAX_COMPUTE_UNIFORM_COMPONENTS);
        for (GLuint i = 0; i < 3; ++i) addIndexedLimit(o, first, "GL_MAX_COMPUTE_WORK_GROUP_COUNT", GL_MAX_COMPUTE_WORK_GROUP_COUNT, i);
        for (GLuint i = 0; i < 3; ++i) addIndexedLimit(o, first, "GL_MAX_COMPUTE_WORK_GROUP_SIZE", GL_MAX_COMPUTE_WORK_GROUP_SIZE, i);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_HEIGHT", GL_MAX_FRAMEBUFFER_HEIGHT);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_SAMPLES", GL_MAX_FRAMEBUFFER_SAMPLES);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_WIDTH", GL_MAX_FRAMEBUFFER_WIDTH);
        addLimit(o, first, "GL_MAX_IMAGE_UNITS", GL_MAX_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET", GL_MAX_PROGRAM_TEXTURE_GATHER_OFFSET);
        addLimit(o, first, "GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET", GL_MIN_PROGRAM_TEXTURE_GATHER_OFFSET);
        addLimit(o, first, "GL_MAX_SAMPLE_MASK_WORDS", GL_MAX_SAMPLE_MASK_WORDS);
        addLimit64(o, first, "GL_MAX_SHADER_STORAGE_BLOCK_SIZE", GL_MAX_SHADER_STORAGE_BLOCK_SIZE);
        addLimit(o, first, "GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS", GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS);
        addLimit(o, first, "GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT", GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
        addLimit(o, first, "GL_MAX_UNIFORM_LOCATIONS", GL_MAX_UNIFORM_LOCATIONS);
        addLimit(o, first, "GL_MAX_VERTEX_ATTRIB_BINDINGS", GL_MAX_VERTEX_ATTRIB_BINDINGS);
        addLimit(o, first, "GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET", GL_MAX_VERTEX_ATTRIB_RELATIVE_OFFSET);
        addLimit(o, first, "GL_MAX_VERTEX_ATTRIB_STRIDE", GL_MAX_VERTEX_ATTRIB_STRIDE);
    }

    if (glCode >= 320) {
        addFloatLimit(o, first, "GL_MIN_SAMPLE_SHADING_VALUE", GL_MIN_SAMPLE_SHADING_VALUE);
        addLimit64(o, first, "GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS", GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS);
        addLimit64(o, first, "GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS", GL_MAX_COMBINED_TESS_CONTROL_UNIFORM_COMPONENTS);
        addLimit64(o, first, "GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS", GL_MAX_COMBINED_TESS_EVALUATION_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_DEBUG_GROUP_STACK_DEPTH", GL_MAX_DEBUG_GROUP_STACK_DEPTH);
        addLimit(o, first, "GL_MAX_DEBUG_LOGGED_MESSAGES", GL_MAX_DEBUG_LOGGED_MESSAGES);
        addLimit(o, first, "GL_MAX_DEBUG_MESSAGE_LENGTH", GL_MAX_DEBUG_MESSAGE_LENGTH);
        addFloatLimit(o, first, "GL_MIN_FRAGMENT_INTERPOLATION_OFFSET", GL_MIN_FRAGMENT_INTERPOLATION_OFFSET);
        addFloatLimit(o, first, "GL_MAX_FRAGMENT_INTERPOLATION_OFFSET", GL_MAX_FRAGMENT_INTERPOLATION_OFFSET);
        addLimit(o, first, "GL_MAX_FRAMEBUFFER_LAYERS", GL_MAX_FRAMEBUFFER_LAYERS);
        addLimit(o, first, "GL_MAX_LABEL_LENGTH", GL_MAX_LABEL_LENGTH);
        addLimit(o, first, "GL_MAX_PATCH_VERTICES", GL_MAX_PATCH_VERTICES);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS", GL_MAX_TESS_CONTROL_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS", GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS", GL_MAX_TESS_CONTROL_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_INPUT_COMPONENTS", GL_MAX_TESS_CONTROL_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS", GL_MAX_TESS_CONTROL_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS", GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS", GL_MAX_TESS_CONTROL_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS", GL_MAX_TESS_CONTROL_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS", GL_MAX_TESS_CONTROL_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS", GL_MAX_TESS_CONTROL_TOTAL_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS", GL_MAX_TESS_EVALUATION_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS", GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS", GL_MAX_TESS_EVALUATION_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS", GL_MAX_TESS_EVALUATION_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS", GL_MAX_TESS_EVALUATION_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS", GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS", GL_MAX_TESS_EVALUATION_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS", GL_MAX_TESS_EVALUATION_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS", GL_MAX_TESS_EVALUATION_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_ATOMIC_COUNTERS", GL_MAX_GEOMETRY_ATOMIC_COUNTERS);
        addLimit(o, first, "GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS", GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS);
        addLimit(o, first, "GL_MAX_GEOMETRY_IMAGE_UNIFORMS", GL_MAX_GEOMETRY_IMAGE_UNIFORMS);
        addLimit(o, first, "GL_MAX_GEOMETRY_INPUT_COMPONENTS", GL_MAX_GEOMETRY_INPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_OUTPUT_COMPONENTS", GL_MAX_GEOMETRY_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_OUTPUT_VERTICES", GL_MAX_GEOMETRY_OUTPUT_VERTICES);
        addLimit(o, first, "GL_MAX_GEOMETRY_SHADER_INVOCATIONS", GL_MAX_GEOMETRY_SHADER_INVOCATIONS);
        addLimit(o, first, "GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS", GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS);
        addLimit(o, first, "GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS", GL_MAX_GEOMETRY_TEXTURE_IMAGE_UNITS);
        addLimit(o, first, "GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS", GL_MAX_GEOMETRY_TOTAL_OUTPUT_COMPONENTS);
        addLimit(o, first, "GL_MAX_GEOMETRY_UNIFORM_BLOCKS", GL_MAX_GEOMETRY_UNIFORM_BLOCKS);
        addLimit(o, first, "GL_MAX_GEOMETRY_UNIFORM_COMPONENTS", GL_MAX_GEOMETRY_UNIFORM_COMPONENTS);
        addLimit(o, first, "GL_MAX_TESS_GEN_LEVEL", GL_MAX_TESS_GEN_LEVEL);
        addLimit(o, first, "GL_MAX_TESS_PATCH_COMPONENTS", GL_MAX_TESS_PATCH_COMPONENTS);
        addLimit(o, first, "GL_MAX_TEXTURE_BUFFER_SIZE", GL_MAX_TEXTURE_BUFFER_SIZE);
        addFloatLimit2(o, first, "GL_MULTISAMPLE_LINE_WIDTH_RANGE", GL_MULTISAMPLE_LINE_WIDTH_RANGE);
        addFloatLimit(o, first, "GL_MULTISAMPLE_LINE_WIDTH_GRANULARITY", GL_MULTISAMPLE_LINE_WIDTH_GRANULARITY);
        addLimit(o, first, "GL_FRAGMENT_INTERPOLATION_OFFSET_BITS", GL_FRAGMENT_INTERPOLATION_OFFSET_BITS);
        addHexLimit(o, first, "GL_LAYER_PROVOKING_VERTEX", GL_LAYER_PROVOKING_VERTEX);
        addBooleanLimit(o, first, "GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED", GL_PRIMITIVE_RESTART_FOR_PATCHES_SUPPORTED);
        addLimit(o, first, "GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT", GL_TEXTURE_BUFFER_OFFSET_ALIGNMENT);
    }
    o << ']';

    bool mandatoryEnumerationsComplete = true;
    auto appendEnumArray = [&](const char* key, const char* countName, GLenum countEnum, const char* valuesName, GLenum valuesEnum) {
        GLint count = 0;
        clearGlErrors();
        glGetIntegerv(countEnum, &count);
        const GLenum countError = glGetError();
        const GLenum normalizedCountError = countError == GL_NO_ERROR && count >= 0 && count <= kMaxGlEnumerationCount ? GL_NO_ERROR : countError == GL_NO_ERROR ? GL_INVALID_VALUE : countError;
        diagnostic(countName, normalizedCountError);
        std::vector<GLint> values;
        GLenum valueError = GL_NO_ERROR;
        if (normalizedCountError == GL_NO_ERROR) {
            values.resize(static_cast<size_t>(count));
            if (count > 0) {
                clearGlErrors();
                glGetIntegerv(valuesEnum, values.data());
                valueError = glGetError();
                if (valueError != GL_NO_ERROR) values.clear();
                diagnostic(valuesName, valueError);
            } else {
                if (activeDiagnostics) activeDiagnostics->push_back({valuesName, "Not applicable", "Count is zero"});
            }
        } else {
            if (activeDiagnostics) activeDiagnostics->push_back({valuesName, "Unavailable", std::string(countName) + " unavailable"});
            valueError = normalizedCountError;
        }
        if (normalizedCountError != GL_NO_ERROR || valueError != GL_NO_ERROR) mandatoryEnumerationsComplete = false;
        diagnostic(key, normalizedCountError != GL_NO_ERROR ? normalizedCountError : valueError);
        o << ",\"" << key << "\":[";
        for (size_t i = 0; i < values.size(); ++i) {
            if (i) o << ',';
            o << q(enumDisplay(values[i], key));
        }
        o << ']';
    };

    appendEnumArray("shaderBinaryFormats", "GL_NUM_SHADER_BINARY_FORMATS", GL_NUM_SHADER_BINARY_FORMATS, "GL_SHADER_BINARY_FORMATS", GL_SHADER_BINARY_FORMATS);
    if (glCode >= 300 || hasExt(glExt, "GL_OES_get_program_binary")) {
        appendEnumArray("programBinaryFormats", "GL_NUM_PROGRAM_BINARY_FORMATS", GL_NUM_PROGRAM_BINARY_FORMATS, "GL_PROGRAM_BINARY_FORMATS", GL_PROGRAM_BINARY_FORMATS);
    } else {
        diagnostics.push_back({"programBinaryFormats", "Not applicable", "Requires OpenGL ES 3.0 or GL_OES_get_program_binary"});
        o << ",\"programBinaryFormats\":[]";
    }

    GLint nfmt = 0;
    clearGlErrors();
    glGetIntegerv(GL_NUM_COMPRESSED_TEXTURE_FORMATS, &nfmt);
    const GLenum compressedCountError = glGetError();
    const GLenum normalizedCompressedCountError = compressedCountError == GL_NO_ERROR && nfmt >= 0 && nfmt <= kMaxGlEnumerationCount ? GL_NO_ERROR : compressedCountError == GL_NO_ERROR ? GL_INVALID_VALUE : compressedCountError;
    diagnostic("GL_NUM_COMPRESSED_TEXTURE_FORMATS", normalizedCompressedCountError);
    std::vector<GLint> fmts;
    GLenum compressedValuesError = GL_NO_ERROR;
    if (normalizedCompressedCountError == GL_NO_ERROR) {
        fmts.resize(static_cast<size_t>(nfmt));
        if (nfmt > 0) {
            clearGlErrors();
            glGetIntegerv(GL_COMPRESSED_TEXTURE_FORMATS, fmts.data());
            compressedValuesError = glGetError();
            if (compressedValuesError != GL_NO_ERROR) fmts.clear();
            diagnostic("GL_COMPRESSED_TEXTURE_FORMATS", compressedValuesError);
        } else {
            if (activeDiagnostics) activeDiagnostics->push_back({"GL_COMPRESSED_TEXTURE_FORMATS", "Not applicable", "Count is zero"});
        }
    } else {
        if (activeDiagnostics) activeDiagnostics->push_back({"GL_COMPRESSED_TEXTURE_FORMATS", "Unavailable", "GL_NUM_COMPRESSED_TEXTURE_FORMATS unavailable"});
        compressedValuesError = normalizedCompressedCountError;
    }
    if (normalizedCompressedCountError != GL_NO_ERROR || compressedValuesError != GL_NO_ERROR) mandatoryEnumerationsComplete = false;
    diagnostic("compressedFormats", normalizedCompressedCountError != GL_NO_ERROR ? normalizedCompressedCountError : compressedValuesError);
    o << ",\"compressedFormats\":[";
    for (size_t i = 0; i < fmts.size(); ++i) {
        if (i) o << ',';
        o << q(enumDisplay(fmts[i], "compressedFormats"));
    }
    o << ']';

    if (!mandatoryEnumerationsComplete) {
        diagnosticsScope.reset();
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"A required GL format/binary enumeration failed; an empty capability array was not fabricated\"}");
    }
    o << ",\"internalFormats\":[";
    bool internalFormatFirst = true;
    if (glCode >= 300) {
        const auto nvInternalFormatQuery = hasExt(glExt, "GL_NV_internalformat_sample_query")
            ? reinterpret_cast<GetInternalformatSampleivNvProc>(eglGetProcAddress("glGetInternalformatSampleivNV"))
            : nullptr;
        for (const auto& format : kRenderableInternalFormats) {
            appendInternalFormatEntry(o, internalFormatFirst, GL_RENDERBUFFER, "GL_RENDERBUFFER", format, nvInternalFormatQuery);
        }
        if (glCode >= 310) {
            for (const auto& format : kRenderableInternalFormats) {
                appendInternalFormatEntry(o, internalFormatFirst, GL_TEXTURE_2D_MULTISAMPLE, "GL_TEXTURE_2D_MULTISAMPLE", format, nvInternalFormatQuery);
            }
        }
        if (glCode >= 320 || hasExt(glExt, "GL_OES_texture_storage_multisample_2d_array")) {
            const GLenum arrayTarget = glCode >= 320 ? GL_TEXTURE_2D_MULTISAMPLE_ARRAY : static_cast<GLenum>(0x9102);
            const char* arrayTargetName = glCode >= 320 ? "GL_TEXTURE_2D_MULTISAMPLE_ARRAY" : "GL_TEXTURE_2D_MULTISAMPLE_ARRAY_OES";
            for (const auto& format : kRenderableInternalFormats) {
                appendInternalFormatEntry(o, internalFormatFirst, arrayTarget, arrayTargetName, format, nvInternalFormatQuery);
            }
        }
        if (hasExt(glExt, "GL_NV_internalformat_sample_query") && !nvInternalFormatQuery && activeDiagnostics) {
            activeDiagnostics->push_back({"glGetInternalformatSampleivNV", "Unavailable", "Extension advertised but entry point was unavailable"});
        }
    } else if (activeDiagnostics) {
        activeDiagnostics->push_back({"internalFormats", "Not applicable", "Requires OpenGL ES 3.0+"});
    }
    o << ']';

    const GLenum shaders[] = {GL_VERTEX_SHADER, GL_FRAGMENT_SHADER};
    const char* shaderNames[] = {"GL_VERTEX_SHADER", "GL_FRAGMENT_SHADER"};
    const GLenum precisions[] = {GL_LOW_FLOAT, GL_MEDIUM_FLOAT, GL_HIGH_FLOAT, GL_LOW_INT, GL_MEDIUM_INT, GL_HIGH_INT};
    const char* precisionNames[] = {"GL_LOW_FLOAT", "GL_MEDIUM_FLOAT", "GL_HIGH_FLOAT", "GL_LOW_INT", "GL_MEDIUM_INT", "GL_HIGH_INT"};
    o << ",\"precision\":[";
    bool pf = true;
    for (int si = 0; si < 2; ++si) {
        for (int pi = 0; pi < 6; ++pi) {
            GLint range[2] = {0, 0};
            GLint precision = 0;
            clearGlErrors();
            glGetShaderPrecisionFormat(shaders[si], precisions[pi], range, &precision);
            const GLenum precisionError = glGetError();
            const std::string precisionQuery = std::string(shaderNames[si]) + "/" + precisionNames[pi];
            diagnostic(precisionQuery.c_str(), precisionError);
            if (precisionError != GL_NO_ERROR) continue;
            if (!pf) o << ',';
            pf = false;
            o << "{\"shader\":" << q(shaderNames[si]) << ",\"type\":" << q(precisionNames[pi])
              << ",\"rangeMin\":" << range[0] << ",\"rangeMax\":" << range[1] << ",\"precision\":" << precision << '}';
        }
    }
    o << ']';

    reconcileDiagnosticIdentities(diagnostics);
    o << ",\"diagnostics\":[";
    for (size_t i = 0; i < diagnostics.size(); ++i) {
        if (i) o << ',';
        o << "{\"name\":" << q(diagnostics[i].name) << ",\"status\":" << q(diagnostics[i].status) << ",\"detail\":" << q(diagnostics[i].detail) << '}';
    }
    o << ']';
    diagnosticsScope.reset();

    EGLint totalConfigs = 0;
    if (eglGetConfigs(d, nullptr, 0, &totalConfigs) != EGL_TRUE || totalConfigs <= 0 || totalConfigs > kMaxEglConfigCount) {
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL configuration enumeration failed or exceeded the 4096-entry safety bound\"}");
    }
    const EGLint configCapacity = totalConfigs;
    std::vector<EGLConfig> configs(static_cast<size_t>(configCapacity));
    if (eglGetConfigs(d, configs.data(), configCapacity, &totalConfigs) != EGL_TRUE || totalConfigs <= 0 || totalConfigs > configCapacity || totalConfigs != configCapacity || totalConfigs > kMaxEglConfigCount) {
        releaseEgl(d, s, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL configuration data could not be read safely\"}");
    }
    o << ",\"eglConfigs\":[";
    bool configFirst = true;
    const bool hasRecordableConfigAttr = hasExt(displayExt, "EGL_ANDROID_recordable");
    const bool hasFramebufferTargetConfigAttr = hasExt(displayExt, "EGL_ANDROID_framebuffer_target");
    const bool hasFloatComponentsConfigAttr = hasExt(displayExt, "EGL_EXT_pixel_format_float");
    for (EGLint i = 0; i < totalConfigs; ++i) {
        const EGLConfig ec = configs[static_cast<size_t>(i)];
        const auto idResult = queryConfigAttr(d, ec, EGL_CONFIG_ID);
        if (!idResult.available) {
            releaseEgl(d, s, c);
            return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"An EGL configuration could not be identified\"}");
        }
        const EGLint id = idResult.value;
        auto configAttr = [&](EGLint attr, int minVersion) -> EglAttrResult { return eglCode >= minVersion ? queryConfigAttr(d, ec, attr) : notQueried; };
        const EglConfigQuery attrs[] = {
            {"red", configAttr(EGL_RED_SIZE, 100), true, ""}, {"green", configAttr(EGL_GREEN_SIZE, 100), true, ""}, {"blue", configAttr(EGL_BLUE_SIZE, 100), true, ""},
            {"alpha", configAttr(EGL_ALPHA_SIZE, 100), true, ""}, {"depth", configAttr(EGL_DEPTH_SIZE, 100), true, ""}, {"stencil", configAttr(EGL_STENCIL_SIZE, 100), true, ""},
            {"sampleBuffers", configAttr(EGL_SAMPLE_BUFFERS, 100), true, ""}, {"samples", configAttr(EGL_SAMPLES, 100), true, ""}, {"surfaceType", configAttr(EGL_SURFACE_TYPE, 100), true, ""},
            {"renderableType", configAttr(EGL_RENDERABLE_TYPE, 120), eglCode >= 120, "Requires EGL 1.2+"},
            {"conformant", configAttr(EGL_CONFORMANT, 130), eglCode >= 130, "Requires EGL 1.3+"},
            {"configCaveat", configAttr(EGL_CONFIG_CAVEAT, 100), true, ""},
            {"colorBufferType", configAttr(EGL_COLOR_BUFFER_TYPE, 120), eglCode >= 120, "Requires EGL 1.2+"},
            {"level", configAttr(EGL_LEVEL, 100), true, ""}, {"nativeRenderable", configAttr(EGL_NATIVE_RENDERABLE, 100), true, ""},
            {"nativeVisualId", configAttr(EGL_NATIVE_VISUAL_ID, 100), true, ""},
            {"minSwapInterval", configAttr(EGL_MIN_SWAP_INTERVAL, 110), eglCode >= 110, "Requires EGL 1.1+"},
            {"maxSwapInterval", configAttr(EGL_MAX_SWAP_INTERVAL, 110), eglCode >= 110, "Requires EGL 1.1+"},
            {"bufferSize", configAttr(EGL_BUFFER_SIZE, 100), true, ""},
            {"luminanceSize", configAttr(EGL_LUMINANCE_SIZE, 120), eglCode >= 120, "Requires EGL 1.2+"},
            {"alphaMaskSize", configAttr(EGL_ALPHA_MASK_SIZE, 120), eglCode >= 120, "Requires EGL 1.2+"},
            {"bindToTextureRgb", configAttr(EGL_BIND_TO_TEXTURE_RGB, 110), eglCode >= 110, "Requires EGL 1.1+"},
            {"bindToTextureRgba", configAttr(EGL_BIND_TO_TEXTURE_RGBA, 110), eglCode >= 110, "Requires EGL 1.1+"},
            {"maxPbufferWidth", configAttr(EGL_MAX_PBUFFER_WIDTH, 100), true, ""}, {"maxPbufferHeight", configAttr(EGL_MAX_PBUFFER_HEIGHT, 100), true, ""}, {"maxPbufferPixels", configAttr(EGL_MAX_PBUFFER_PIXELS, 100), true, ""},
            {"nativeVisualType", configAttr(EGL_NATIVE_VISUAL_TYPE, 100), true, ""}, {"transparentType", configAttr(EGL_TRANSPARENT_TYPE, 100), true, ""},
            {"transparentRed", configAttr(EGL_TRANSPARENT_RED_VALUE, 100), true, ""}, {"transparentGreen", configAttr(EGL_TRANSPARENT_GREEN_VALUE, 100), true, ""}, {"transparentBlue", configAttr(EGL_TRANSPARENT_BLUE_VALUE, 100), true, ""}
        };
        auto attr = [&](size_t index) -> EglAttrResult { return attrs[index].result; };
        const auto red = attr(0), green = attr(1), blue = attr(2), alpha = attr(3), depth = attr(4), stencil = attr(5);
        const auto sampleBuffers = attr(6), samples = attr(7), surfaceType = attr(8), renderableType = attr(9), conformant = attr(10);
        const auto caveat = attr(11), colorBufferType = attr(12), level = attr(13), nativeRenderable = attr(14), nativeVisualId = attr(15);
        const auto minSwap = attr(16), maxSwap = attr(17), bufferSize = attr(18), luminanceSize = attr(19), alphaMaskSize = attr(20);
        const auto bindRgb = attr(21), bindRgba = attr(22), maxPbufferWidth = attr(23), maxPbufferHeight = attr(24), maxPbufferPixels = attr(25);
        const auto nativeVisualType = attr(26), transparentType = attr(27), transparentRed = attr(28), transparentGreen = attr(29), transparentBlue = attr(30);
        const auto recordable = hasRecordableConfigAttr ? queryConfigAttr(d, ec, EGL_RECORDABLE_ANDROID_VALUE) : EglAttrResult{0, false, EGL_SUCCESS};
        const auto framebufferTarget = hasFramebufferTargetConfigAttr ? queryConfigAttr(d, ec, EGL_FRAMEBUFFER_TARGET_ANDROID_VALUE) : EglAttrResult{0, false, EGL_SUCCESS};
        const auto colorComponentType = hasFloatComponentsConfigAttr ? queryConfigAttr(d, ec, EGL_COLOR_COMPONENT_TYPE_EXT_VALUE) : EglAttrResult{0, false, EGL_SUCCESS};
        if (!configFirst) o << ',';
        configFirst = false;
        o << "{\"id\":" << id
          << ",\"red\":" << (red.available ? std::to_string(red.value) : "null")
          << ",\"green\":" << (green.available ? std::to_string(green.value) : "null")
          << ",\"blue\":" << (blue.available ? std::to_string(blue.value) : "null")
          << ",\"alpha\":" << (alpha.available ? std::to_string(alpha.value) : "null")
          << ",\"depth\":" << (depth.available ? std::to_string(depth.value) : "null")
          << ",\"stencil\":" << (stencil.available ? std::to_string(stencil.value) : "null")
          << ",\"sampleBuffers\":" << (sampleBuffers.available ? std::to_string(sampleBuffers.value) : "null")
          << ",\"samples\":" << (samples.available ? std::to_string(samples.value) : "null")
          << ",\"surfaceType\":" << (surfaceType.available ? q(hexv(surfaceType.value)) : "null")
          << ",\"renderableType\":" << (renderableType.available ? q(hexv(renderableType.value)) : "null")
          << ",\"conformant\":" << (conformant.available ? q(hexv(conformant.value)) : "null")
          << ",\"configCaveat\":" << (caveat.available ? q(hexv(caveat.value)) : "null")
          << ",\"colorBufferType\":" << (colorBufferType.available ? q(hexv(colorBufferType.value)) : "null")
          << ",\"level\":" << (level.available ? std::to_string(level.value) : "null")
          << ",\"nativeRenderable\":" << (nativeRenderable.available ? std::to_string(nativeRenderable.value) : "null")
          << ",\"nativeVisualId\":" << (nativeVisualId.available ? std::to_string(nativeVisualId.value) : "null")
          << ",\"minSwapInterval\":" << (minSwap.available ? std::to_string(minSwap.value) : "null")
          << ",\"maxSwapInterval\":" << (maxSwap.available ? std::to_string(maxSwap.value) : "null")
          << ",\"bufferSize\":" << (bufferSize.available ? std::to_string(bufferSize.value) : "null")
          << ",\"luminanceSize\":" << (luminanceSize.available ? std::to_string(luminanceSize.value) : "null")
          << ",\"alphaMaskSize\":" << (alphaMaskSize.available ? std::to_string(alphaMaskSize.value) : "null")
          << ",\"bindToTextureRgb\":" << (bindRgb.available ? std::to_string(bindRgb.value) : "null")
          << ",\"bindToTextureRgba\":" << (bindRgba.available ? std::to_string(bindRgba.value) : "null")
          << ",\"maxPbufferWidth\":" << (maxPbufferWidth.available ? std::to_string(maxPbufferWidth.value) : "null")
          << ",\"maxPbufferHeight\":" << (maxPbufferHeight.available ? std::to_string(maxPbufferHeight.value) : "null")
          << ",\"maxPbufferPixels\":" << (maxPbufferPixels.available ? std::to_string(maxPbufferPixels.value) : "null")
          << ",\"nativeVisualType\":" << (nativeVisualType.available ? std::to_string(nativeVisualType.value) : "null")
          << ",\"transparentType\":" << (transparentType.available ? q(hexv(transparentType.value)) : "null")
          << ",\"transparentRed\":" << (transparentRed.available ? std::to_string(transparentRed.value) : "null")
          << ",\"transparentGreen\":" << (transparentGreen.available ? std::to_string(transparentGreen.value) : "null")
          << ",\"transparentBlue\":" << (transparentBlue.available ? std::to_string(transparentBlue.value) : "null")
          << ",\"recordableAndroid\":" << (hasRecordableConfigAttr && recordable.available ? std::to_string(recordable.value) : "null")
          << ",\"framebufferTargetAndroid\":" << (hasFramebufferTargetConfigAttr && framebufferTarget.available ? std::to_string(framebufferTarget.value) : "null")
          << ",\"colorComponentTypeExt\":" << (hasFloatComponentsConfigAttr && colorComponentType.available ? q(eglEnumDisplay(colorComponentType.value)) : "null")
          << ",\"unavailableAttributes\":[";
        bool unavailableFirst = true;
        for (const auto& item : attrs) {
            if (item.result.available) continue;
            if (!unavailableFirst) o << ',';
            unavailableFirst = false;
            o << "{\"name\":" << q(item.key) << ",\"error\":" << q(item.applicable ? eglErrorDisplay(item.result.error) : std::string("Not applicable: ") + item.notApplicableDetail) << '}';
        }
        const std::pair<const char*, EglAttrResult> extensionAttrs[] = {{"EGL_RECORDABLE_ANDROID", recordable}, {"EGL_FRAMEBUFFER_TARGET_ANDROID", framebufferTarget}, {"EGL_COLOR_COMPONENT_TYPE_EXT", colorComponentType}};
        const bool extensionApplies[] = {hasRecordableConfigAttr, hasFramebufferTargetConfigAttr, hasFloatComponentsConfigAttr};
        for (size_t extensionIndex = 0; extensionIndex < 3; ++extensionIndex) {
            if (!extensionApplies[extensionIndex] || extensionAttrs[extensionIndex].second.available) continue;
            if (!unavailableFirst) o << ',';
            unavailableFirst = false;
            o << "{\"name\":" << q(extensionAttrs[extensionIndex].first) << ",\"error\":" << q(eglErrorDisplay(extensionAttrs[extensionIndex].second.error)) << '}';
        }
        o << "]}";
    }
    o << "]}";

    releaseEgl(d, s, c);
    const std::string result = o.str();
    return env->NewStringUTF(result.c_str());
}

static std::string shaderInfoLog(GLuint shader) {
    GLint length = 0;
    clearGlErrors();
    glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &length);
    if (glGetError() != GL_NO_ERROR || length < 0) return "Shader info log length query failed";
    if (length <= 1) return "";
    if (length > kMaxInfoLogBytes) return "Shader info log exceeded the 1 MiB safety bound";
    std::vector<char> buffer(static_cast<size_t>(length), '\0');
    GLsizei written = 0;
    clearGlErrors();
    glGetShaderInfoLog(shader, length, &written, buffer.data());
    if (glGetError() != GL_NO_ERROR || written < 0 || written >= length)
        return "Shader info log read failed or returned an invalid length";
    return std::string(buffer.data(), static_cast<size_t>(written));
}

static std::string programInfoLog(GLuint program) {
    GLint length = 0;
    clearGlErrors();
    glGetProgramiv(program, GL_INFO_LOG_LENGTH, &length);
    if (glGetError() != GL_NO_ERROR || length < 0) return "Program info log length query failed";
    if (length <= 1) return "";
    if (length > kMaxInfoLogBytes) return "Program info log exceeded the 1 MiB safety bound";
    std::vector<char> buffer(static_cast<size_t>(length), '\0');
    GLsizei written = 0;
    clearGlErrors();
    glGetProgramInfoLog(program, length, &written, buffer.data());
    if (glGetError() != GL_NO_ERROR || written < 0 || written >= length)
        return "Program info log read failed or returned an invalid length";
    return std::string(buffer.data(), static_cast<size_t>(written));
}

static thread_local bool selfTestDebugCallbackSeen = false;
static void selfTestDebugCallback(GLenum, GLenum, GLuint, GLenum, GLsizei, const GLchar*, const void*) { selfTestDebugCallbackSeen = true; }

extern "C" JNIEXPORT jstring JNICALL
Java_com_efishell_openglesscope_OpenGLESProbeService_nativeSelfTest(JNIEnv* env, jobject) {
    EGLDisplay d = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (d == EGL_NO_DISPLAY) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"EGL display unavailable\",\"tests\":[]}"); }
    EGLint eglMajor = 0;
    EGLint eglMinor = 0;
    if (eglInitialize(d, &eglMajor, &eglMinor) != EGL_TRUE) { eglReleaseThread(); return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"eglInitialize failed\",\"tests\":[]}"); }
    EGLConfig cfg = nullptr;
    bool selfTestDisplayExtComplete = false;
    eglGetError();
    const char* selfTestDisplayExtensions = eglQueryString(d, EGL_EXTENSIONS);
    const EGLint selfTestExtensionsError = eglGetError();
    const auto displayExt = selfTestExtensionsError == EGL_SUCCESS ? splitExt(selfTestDisplayExtensions, &selfTestDisplayExtComplete) : std::vector<std::string>{};
    if (!selfTestDisplayExtComplete) {
        releaseEgl(d, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Self-test EGL display extension enumeration failed\",\"tests\":[]}");
    }
    EGLContext c = createBestContext(d, cfg, eglMajor * 100 + eglMinor * 10, displayExt);
    if (c == EGL_NO_CONTEXT || cfg == nullptr) {
        releaseEgl(d, EGL_NO_SURFACE, EGL_NO_CONTEXT);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES context creation failed\",\"tests\":[]}");
    }
    const EGLint pbAttrs[] = {EGL_WIDTH, 1, EGL_HEIGHT, 1, EGL_NONE};
    EGLSurface surface = eglCreatePbufferSurface(d, cfg, pbAttrs);
    if (surface == EGL_NO_SURFACE || eglMakeCurrent(d, surface, surface, c) != EGL_TRUE) {
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"OpenGL ES self-test context could not be made current\",\"tests\":[]}");
    }
    const char* vendorText = queryGlString(GL_VENDOR, "GL_VENDOR");
    const char* rendererText = queryGlString(GL_RENDERER, "GL_RENDERER");
    const char* versionText = queryGlString(GL_VERSION, "GL_VERSION");
    if (!vendorText || !rendererText || !versionText) {
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Self-test OpenGL ES identity strings were unavailable\",\"tests\":[]}");
    }
    const std::string runtimeVendor = vendorText;
    const std::string runtimeRenderer = rendererText;
    const std::string runtimeVersion = versionText;
    const auto parsed = runtimeGlVersion(versionText);
    if (parsed.first < 2 || parsed.second < 0 || parsed.second > 9) {
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Self-test runtime GL version identity was inconsistent\",\"tests\":[]}");
    }
    const int glCode = versionCode(parsed);
    bool selfTestGlExtComplete = false;
    const auto extensions = glExtensions(glCode, selfTestGlExtComplete);
    if (!selfTestGlExtComplete) {
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"unavailable\",\"reason\":\"Self-test GL extension enumeration failed\",\"tests\":[]}");
    }
    auto hasRuntimeExtension = [&](const char* name) { return hasExt(extensions, name); };
    std::vector<std::string> tests;
    const char* vertexSource = "attribute vec4 aPosition; void main(){gl_Position=aPosition;}";
    const char* fragmentSource = "precision mediump float; void main(){gl_FragColor=vec4(1.0,0.0,1.0,1.0);}";
    GLuint vertex = glCreateShader(GL_VERTEX_SHADER);
    GLuint fragment = glCreateShader(GL_FRAGMENT_SHADER);
    if (vertex == 0 || fragment == 0) {
        if (vertex != 0) glDeleteShader(vertex);
        if (fragment != 0) glDeleteShader(fragment);
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"completed_with_failures\",\"reason\":\"Shader object creation failed\",\"tests\":[{\"name\":\"Shader object creation\",\"status\":\"FAIL\",\"detail\":\"glCreateShader returned zero\"}]}");
    }
    glShaderSource(vertex, 1, &vertexSource, nullptr);
    glShaderSource(fragment, 1, &fragmentSource, nullptr);
    glCompileShader(vertex);
    glCompileShader(fragment);
    GLint vertexOk = GL_FALSE;
    GLint fragmentOk = GL_FALSE;
    glGetShaderiv(vertex, GL_COMPILE_STATUS, &vertexOk);
    glGetShaderiv(fragment, GL_COMPILE_STATUS, &fragmentOk);
    tests.push_back(std::string("{\"name\":\"Minimal vertex shader compile\",\"status\":") + q(vertexOk == GL_TRUE ? "PASS" : "FAIL") + ",\"detail\":" + q(shaderInfoLog(vertex)) + "}");
    tests.push_back(std::string("{\"name\":\"Minimal fragment shader compile\",\"status\":") + q(fragmentOk == GL_TRUE ? "PASS" : "FAIL") + ",\"detail\":" + q(shaderInfoLog(fragment)) + "}");
    GLuint program = glCreateProgram();
    if (program == 0) {
        glDeleteShader(vertex);
        glDeleteShader(fragment);
        releaseEgl(d, surface, c);
        return env->NewStringUTF("{\"status\":\"completed_with_failures\",\"reason\":\"Program object creation failed\",\"tests\":[{\"name\":\"Program object creation\",\"status\":\"FAIL\",\"detail\":\"glCreateProgram returned zero\"}]}");
    }
    if (vertexOk == GL_TRUE && fragmentOk == GL_TRUE) {
        glAttachShader(program, vertex);
        glAttachShader(program, fragment);
        if (glCode >= 300) glProgramParameteri(program, GL_PROGRAM_BINARY_RETRIEVABLE_HINT, GL_TRUE);
        glLinkProgram(program);
    }
    GLint linkOk = GL_FALSE;
    glGetProgramiv(program, GL_LINK_STATUS, &linkOk);
    tests.push_back(std::string("{\"name\":\"Minimal program link\",\"status\":") + q(linkOk == GL_TRUE ? "PASS" : "FAIL") + ",\"detail\":" + q(programInfoLog(program)) + "}");
    if (linkOk == GL_TRUE && (glCode >= 300 || hasRuntimeExtension("GL_OES_get_program_binary"))) {
        GLint length = 0;
        glGetProgramiv(program, GL_PROGRAM_BINARY_LENGTH, &length);
        if (length > 0 && length <= kMaxProgramBinaryBytes) {
            std::vector<unsigned char> binary(static_cast<size_t>(length));
            GLsizei written = 0;
            GLenum format = 0;
            bool roundTripAttempted = false;
            bool roundTripPassed = false;
            std::string detail;
            if (glCode >= 300) {
                glGetProgramBinary(program, length, &written, &format, binary.data());
                if (written > 0 && written <= length) {
                    GLuint restored = glCreateProgram();
                    if (restored != 0) glProgramBinary(restored, format, binary.data(), written);
                    GLint restoredOk = GL_FALSE;
                    if (restored != 0) glGetProgramiv(restored, GL_LINK_STATUS, &restoredOk);
                    roundTripAttempted = true;
                    roundTripPassed = restoredOk == GL_TRUE;
                    detail = restored != 0 ? programInfoLog(restored) : "glCreateProgram returned zero";
                    if (restored != 0) glDeleteProgram(restored);
                }
            } else {
                using GetProgramBinaryOES = void (*)(GLuint, GLsizei, GLsizei*, GLenum*, void*);
                using ProgramBinaryOES = void (*)(GLuint, GLenum, const void*, GLint);
                auto getBinary = reinterpret_cast<GetProgramBinaryOES>(eglGetProcAddress("glGetProgramBinaryOES"));
                auto setBinary = reinterpret_cast<ProgramBinaryOES>(eglGetProcAddress("glProgramBinaryOES"));
                if (getBinary && setBinary) {
                    getBinary(program, length, &written, &format, binary.data());
                    if (written > 0 && written <= length) {
                        GLuint restored = glCreateProgram();
                        if (restored != 0) setBinary(restored, format, binary.data(), written);
                        GLint restoredOk = GL_FALSE;
                        if (restored != 0) glGetProgramiv(restored, GL_LINK_STATUS, &restoredOk);
                        roundTripAttempted = true;
                        roundTripPassed = restoredOk == GL_TRUE;
                        detail = restored != 0 ? programInfoLog(restored) : "glCreateProgram returned zero";
                        if (restored != 0) glDeleteProgram(restored);
                    }
                }
            }
            tests.push_back(std::string("{\"name\":\"Program binary round-trip\",\"status\":") + q(roundTripAttempted ? (roundTripPassed ? "PASS" : "FAIL") : "UNAVAILABLE") + ",\"detail\":" + q(detail) + "}");
        } else {
            tests.push_back("{\"name\":\"Program binary round-trip\",\"status\":\"UNAVAILABLE\",\"detail\":\"Program binary length was unavailable or exceeded the 8 MiB self-test bound.\"}");
        }
    } else {
        tests.push_back("{\"name\":\"Program binary round-trip\",\"status\":\"NOT_APPLICABLE\",\"detail\":\"Program-binary runtime evidence is not available.\"}");
    }
    if (hasRuntimeExtension("GL_KHR_debug") || glCode >= 320) {
        using DebugCallback = void (*)(GLenum, GLenum, GLuint, GLenum, GLsizei, const GLchar*, const void*);
        using DebugMessageCallback = void (*)(DebugCallback, const void*);
        using DebugMessageInsert = void (*)(GLenum, GLenum, GLuint, GLenum, GLsizei, const GLchar*);
        auto callback = reinterpret_cast<DebugMessageCallback>(eglGetProcAddress(hasRuntimeExtension("GL_KHR_debug") ? "glDebugMessageCallbackKHR" : "glDebugMessageCallback"));
        if (!callback) callback = reinterpret_cast<DebugMessageCallback>(eglGetProcAddress("glDebugMessageCallback"));
        auto insert = reinterpret_cast<DebugMessageInsert>(eglGetProcAddress(hasRuntimeExtension("GL_KHR_debug") ? "glDebugMessageInsertKHR" : "glDebugMessageInsert"));
        if (!insert) insert = reinterpret_cast<DebugMessageInsert>(eglGetProcAddress("glDebugMessageInsert"));
        if (callback && insert) {
            clearGlErrors();
            selfTestDebugCallbackSeen = false;
            callback(selfTestDebugCallback, nullptr);
            glEnable(GL_DEBUG_OUTPUT);
            glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
            const char message[] = "OpenGLESScope self-test";
            insert(GL_DEBUG_SOURCE_APPLICATION, GL_DEBUG_TYPE_MARKER, 1, GL_DEBUG_SEVERITY_NOTIFICATION, static_cast<GLsizei>(sizeof(message) - 1), message);
            const GLenum error = glGetError();
            const bool passed = error == GL_NO_ERROR && selfTestDebugCallbackSeen;
            tests.push_back(std::string("{\"name\":\"KHR_debug callback and insertion\",\"status\":") + q(passed ? "PASS" : "FAIL") + ",\"detail\":" + q(passed ? "Debug callback observed the inserted application message." : error == GL_NO_ERROR ? "Debug insertion completed but the callback was not observed." : "Debug callback/insertion generated a GL error.") + "}");
            callback(nullptr, nullptr);
        } else {
            tests.push_back("{\"name\":\"KHR_debug callback and insertion\",\"status\":\"UNAVAILABLE\",\"detail\":\"The required debug callback or insertion entry point was unavailable.\"}");
        }
    } else {
        tests.push_back("{\"name\":\"KHR_debug message insertion\",\"status\":\"NOT_APPLICABLE\",\"detail\":\"GL_KHR_debug is not exposed by this runtime.\"}");
    }
    glDeleteProgram(program);
    glDeleteShader(vertex);
    glDeleteShader(fragment);
    releaseEgl(d, surface, c);
    bool anyFail = false;
    for (const auto& test : tests) if (test.find("\"status\":\"FAIL\"") != std::string::npos) anyFail = true;
    std::ostringstream out;
    out << "{\"status\":" << q(anyFail ? "completed_with_failures" : "completed") << ",\"vendor\":" << q(runtimeVendor) << ",\"renderer\":" << q(runtimeRenderer) << ",\"runtimeVersion\":" << q(runtimeVersion) << ",\"tests\":[";
    for (size_t i = 0; i < tests.size(); ++i) { if (i) out << ','; out << tests[i]; }
    out << "]}";
    return env->NewStringUTF(out.str().c_str());
}
