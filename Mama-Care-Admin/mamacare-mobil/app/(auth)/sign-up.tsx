import React, { useState, useRef } from "react";
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  ScrollView,
  KeyboardAvoidingView,
  Platform,
  ActivityIndicator,
  Animated,
} from "react-native";
import { router } from "expo-router";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import * as SecureStore from "expo-secure-store";

const BASE_URL = process.env.EXPO_PUBLIC_API_URL;
const REGISTER_ENDPOINT = `${BASE_URL}/api/v1/auth/register`;

export default function SignUpScreen() {
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [agreed, setAgreed] = useState(true);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [form, setForm] = useState({
    firstName: "",
    lastName: "",
    email: "",
    password: "",
    confirmPassword: "",
  });

  const toastAnim = useRef(new Animated.Value(-100)).current;
  const [toastMsg, setToastMsg] = useState("");

  const showToast = (msg: string) => {
    setToastMsg(msg);
    Animated.sequence([
      Animated.timing(toastAnim, { toValue: 0, duration: 350, useNativeDriver: true }),
      Animated.delay(3500),
      Animated.timing(toastAnim, { toValue: -100, duration: 350, useNativeDriver: true }),
    ]).start();
  };

  const dismissToast = () => {
    Animated.timing(toastAnim, { toValue: -100, duration: 300, useNativeDriver: true }).start();
  };

  const validate = () => {
    const newErrors: Record<string, string> = {};

    if (!form.firstName.trim())
      newErrors.firstName = "First name is required.";
    else if (form.firstName.trim().length < 2)
      newErrors.firstName = "First name must be at least 2 characters.";

    if (!form.lastName.trim())
      newErrors.lastName = "Last name is required.";
    else if (form.lastName.trim().length < 2)
      newErrors.lastName = "Last name must be at least 2 characters.";

    if (!form.email.trim())
      newErrors.email = "Email address is required.";
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email))
      newErrors.email = "Enter a valid email address.";

    if (!form.password)
      newErrors.password = "Password is required.";
    else if (form.password.length < 8)
      newErrors.password = "Password must be at least 8 characters.";
    else if (!/[A-Z]/.test(form.password))
      newErrors.password = "Must contain at least one uppercase letter.";
    else if (!/[0-9]/.test(form.password))
      newErrors.password = "Must contain at least one number.";

    if (!form.confirmPassword)
      newErrors.confirmPassword = "Please confirm your password.";
    else if (form.password !== form.confirmPassword)
      newErrors.confirmPassword = "Passwords do not match.";

    if (!agreed)
      newErrors.agreed = "You must agree to the Terms of Use and Privacy Policy.";

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const setField = (key: string, value: string) => {
    setForm({ ...form, [key]: value });
    if (errors[key]) setErrors({ ...errors, [key]: "" });
  };

  const handleSignUp = async () => {
    if (!validate()) return;
    setLoading(true);
    try {
      const response = await fetch(REGISTER_ENDPOINT, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          firstName: form.firstName.trim(),
          lastName: form.lastName.trim(),
          email: form.email.trim().toLowerCase(),
          password: form.password,
        }),
      });

      const data = await response.json();

      if (response.ok || response.status === 200 || response.status === 201) {
        await SecureStore.setItemAsync("accessToken", data.accessToken);
        await SecureStore.setItemAsync("refreshToken", data.refreshToken);
        await SecureStore.setItemAsync("firstName", form.firstName.trim());
        await SecureStore.setItemAsync("lastName", form.lastName.trim());
        router.replace("/(onboarding)/welcome");
      } else if (response.status === 409) {
        setErrors({ email: "An account with this email already exists." });
        showToast("An account with this email already exists.");
      } else if (response.status === 400) {
        showToast(data.message || data.details || "Invalid details. Please check your information.");
      } else if (response.status === 500) {
        showToast("Server error. Please try again later.");
      } else {
        showToast(data.message || "Something went wrong. Please try again.");
      }
    } catch (error: any) {
      if (error.message?.includes("Network request failed") || error.message?.includes("fetch")) {
        showToast("No connection. Please check your internet and try again.");
      } else if (error.message?.includes("timeout")) {
        showToast("Request timed out. Please try again.");
      } else {
        showToast("An unexpected error occurred. Please try again.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#fff" }}>
      {/* Toast */}
      <Animated.View style={{
        position: "absolute", top: 0, left: 0, right: 0, zIndex: 999,
        transform: [{ translateY: toastAnim }],
        backgroundColor: "#C62828",
        paddingHorizontal: 20, paddingVertical: 14,
        flexDirection: "row", alignItems: "center", gap: 10,
        shadowColor: "#000", shadowOffset: { width: 0, height: 4 },
        shadowOpacity: 0.2, shadowRadius: 8, elevation: 10,
      }}>
        <Ionicons name="alert-circle-outline" size={20} color="#fff" />
        <Text style={{ color: "#fff", fontSize: 13, fontWeight: "600", flex: 1 }}>{toastMsg}</Text>
        <TouchableOpacity onPress={dismissToast}>
          <Ionicons name="close" size={18} color="#fff" />
        </TouchableOpacity>
      </Animated.View>

      <KeyboardAvoidingView behavior={Platform.OS === "ios" ? "padding" : "height"} style={{ flex: 1 }}>
        <ScrollView
          showsVerticalScrollIndicator={false}
          keyboardShouldPersistTaps="handled"
          contentContainerStyle={{ paddingBottom: 40 }}
        >
          {/* Back */}
          <View style={{ paddingHorizontal: 20, paddingTop: 16 }}>
            <TouchableOpacity onPress={() => router.back()} style={{ width: 38, height: 38, justifyContent: "center" }}>
              <Ionicons name="arrow-back" size={24} color="#111" />
            </TouchableOpacity>
          </View>

          {/* Header */}
          <View style={{ paddingHorizontal: 24, marginTop: 12, marginBottom: 24 }}>
            <Text style={{ fontSize: 28, fontWeight: "800", color: "#111", marginBottom: 6 }}>
              Create your account
            </Text>
            <Text style={{ fontSize: 14, color: "#999" }}>
              Join MamaCare and start your pregnancy journey with us.
            </Text>
          </View>

          <View style={{ paddingHorizontal: 24, gap: 14 }}>
            <Field
              icon="person-outline"
              placeholder="First name"
              value={form.firstName}
              onChangeText={(v: string) => setField("firstName", v)}
              error={errors.firstName}
            />
            <Field
              icon="person-outline"
              placeholder="Last name"
              value={form.lastName}
              onChangeText={(v: string) => setField("lastName", v)}
              error={errors.lastName}
            />
            <Field
              icon="mail-outline"
              placeholder="Email address"
              value={form.email}
              onChangeText={(v: string) => setField("email", v)}
              keyboardType="email-address"
              autoCapitalize="none"
              error={errors.email}
            />
            <PasswordField
              placeholder="Create password"
              value={form.password}
              onChangeText={(v: string) => setField("password", v)}
              show={showPassword}
              onToggle={() => setShowPassword(!showPassword)}
              error={errors.password}
            />
            <PasswordField
              placeholder="Confirm password"
              value={form.confirmPassword}
              onChangeText={(v: string) => setField("confirmPassword", v)}
              show={showConfirm}
              onToggle={() => setShowConfirm(!showConfirm)}
              error={errors.confirmPassword}
            />

            {/* Terms */}
            <View>
              <TouchableOpacity
                onPress={() => {
                  setAgreed(!agreed);
                  if (errors.agreed) setErrors({ ...errors, agreed: "" });
                }}
                style={{ flexDirection: "row", alignItems: "center", gap: 10 }}
              >
                <View style={{
                  width: 20, height: 20, borderRadius: 5, borderWidth: 1.5,
                  borderColor: errors.agreed ? "#E53935" : agreed ? "#2D7A4F" : "#CCC",
                  backgroundColor: agreed ? "#2D7A4F" : "transparent",
                  alignItems: "center", justifyContent: "center",
                }}>
                  {agreed && <Ionicons name="checkmark" size={13} color="#fff" />}
                </View>
                <Text style={{ fontSize: 13, color: "#666", flex: 1 }}>
                  I agree to the{" "}
                  <Text style={{ color: "#2D7A4F", fontWeight: "700" }}>Terms of Use</Text>
                  {" "}and{" "}
                  <Text style={{ color: "#2D7A4F", fontWeight: "700" }}>Privacy Policy.</Text>
                </Text>
              </TouchableOpacity>
              {errors.agreed ? <Text style={errorText}>{errors.agreed}</Text> : null}
            </View>

            {/* Sign Up button */}
            <TouchableOpacity
              onPress={handleSignUp}
              disabled={loading}
              style={{
                backgroundColor: loading ? "#7AAF90" : "#2D7A4F",
                borderRadius: 14, paddingVertical: 17, alignItems: "center", marginTop: 4,
                shadowColor: "#2D7A4F", shadowOffset: { width: 0, height: 6 },
                shadowOpacity: 0.3, shadowRadius: 12, elevation: 6,
              }}
            >
              {loading
                ? <ActivityIndicator color="#fff" />
                : <Text style={{ color: "#fff", fontWeight: "700", fontSize: 16 }}>Sign Up</Text>
              }
            </TouchableOpacity>

            {/* Divider */}
            <View style={{ flexDirection: "row", alignItems: "center", gap: 12 }}>
              <View style={{ flex: 1, height: 1, backgroundColor: "#F0F0F0" }} />
              <Text style={{ color: "#BDBDBD", fontSize: 12 }}>or sign up with</Text>
              <View style={{ flex: 1, height: 1, backgroundColor: "#F0F0F0" }} />
            </View>

            <View style={{ flexDirection: "row", gap: 12 }}>
              <SocialBtn label="Google" icon="logo-google" />
              <SocialBtn label="Apple" icon="logo-apple" />
            </View>

            <View style={{ flexDirection: "row", justifyContent: "center", marginTop: 4 }}>
              <Text style={{ color: "#999", fontSize: 14 }}>Already have an account? </Text>
              <TouchableOpacity onPress={() => router.push("/(auth)/sign-in")}>
                <Text style={{ color: "#2D7A4F", fontWeight: "700", fontSize: 14 }}>Log in</Text>
              </TouchableOpacity>
            </View>
          </View>
        </ScrollView>
      </KeyboardAvoidingView>
    </SafeAreaView>
  );
}

const fieldStyle = {
  flexDirection: "row" as const,
  alignItems: "center" as const,
  borderWidth: 1,
  borderColor: "#EFEFEF",
  borderRadius: 12,
  backgroundColor: "#FAFAFA",
  paddingHorizontal: 14,
  height: 54,
};

const errorText = {
  color: "#E53935",
  fontSize: 12,
  marginTop: 4,
  marginLeft: 4,
};

function Field({ icon, placeholder, value, onChangeText, keyboardType = "default", autoCapitalize = "words", error }: any) {
  return (
    <View>
      <View style={[fieldStyle, { gap: 10 }, error ? { borderColor: "#E53935" } : {}]}>
        <Ionicons name={icon} size={18} color={error ? "#E53935" : "#BDBDBD"} />
        <TextInput
          placeholder={placeholder}
          placeholderTextColor="#BDBDBD"
          value={value}
          onChangeText={onChangeText}
          keyboardType={keyboardType}
          autoCapitalize={autoCapitalize}
          style={{ flex: 1, fontSize: 14, color: "#333" }}
        />
      </View>
      {error ? <Text style={errorText}>{error}</Text> : null}
    </View>
  );
}

function PasswordField({ placeholder, value, onChangeText, show, onToggle, error }: any) {
  return (
    <View>
      <View style={[fieldStyle, { gap: 10 }, error ? { borderColor: "#E53935" } : {}]}>
        <Ionicons name="lock-closed-outline" size={18} color={error ? "#E53935" : "#BDBDBD"} />
        <TextInput
          placeholder={placeholder}
          placeholderTextColor="#BDBDBD"
          value={value}
          onChangeText={onChangeText}
          secureTextEntry={!show}
          autoCapitalize="none"
          style={{ flex: 1, fontSize: 14, color: "#333" }}
        />
        <TouchableOpacity onPress={onToggle}>
          <Ionicons name={show ? "eye-outline" : "eye-off-outline"} size={19} color="#BDBDBD" />
        </TouchableOpacity>
      </View>
      {error ? <Text style={errorText}>{error}</Text> : null}
    </View>
  );
}

function SocialBtn({ label, icon }: { label: string; icon: any }) {
  return (
    <TouchableOpacity style={{
      flex: 1, flexDirection: "row", alignItems: "center", justifyContent: "center",
      gap: 8, borderWidth: 1, borderColor: "#EFEFEF", borderRadius: 12,
      paddingVertical: 13, backgroundColor: "#FAFAFA",
    }}>
      <Ionicons name={icon} size={18} color="#333" />
      <Text style={{ fontWeight: "600", fontSize: 14, color: "#333" }}>{label}</Text>
    </TouchableOpacity>
  );
}
