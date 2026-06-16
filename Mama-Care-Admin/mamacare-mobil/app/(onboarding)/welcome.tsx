import React, { useRef } from "react";
import {
  View, Text, Image, TouchableOpacity,
  ScrollView, Animated, Dimensions,
} from "react-native";
import { router } from "expo-router";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";

const { width } = Dimensions.get("window");

const FEATURES = [
  { icon: "calendar-outline", label: "Weekly pregnancy updates." },
  { icon: "nutrition-outline", label: "Health tips & nutrition" },
  { icon: "walk-outline", label: "Walk & exercise guidance" },
  { icon: "people-outline", label: "Community support" },
  { icon: "notifications-outline", label: "Reminders & appointments" },
];

export default function WelcomeScreen() {
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#fff" }}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 36 }}
      >
        {/* Skip */}
        <View style={{ flexDirection: "row", justifyContent: "flex-end", paddingHorizontal: 20, paddingTop: 16 }}>
          <TouchableOpacity onPress={() => router.replace("/(tabs)/home")}>
            <Text style={{ color: "#2D7A4F", fontSize: 15, fontWeight: "600" }}>Skip</Text>
          </TouchableOpacity>
        </View>

        {/* Hero image with week badge */}
        <View style={{ alignItems: "center", marginTop: 8, position: "relative" }}>
          <Image
            source={require("../../assets/images/6.png")}
            style={{ width: width * 0.75, height: 220 }}
            resizeMode="contain"
          />
          {/* Week badge */}
          <View style={{
            position: "absolute", right: width * 0.1, top: 40,
            width: 64, height: 64, borderRadius: 32,
            backgroundColor: "#fff",
            borderWidth: 3, borderColor: "#2D7A4F",
            alignItems: "center", justifyContent: "center",
            shadowColor: "#000", shadowOffset: { width: 0, height: 2 },
            shadowOpacity: 0.1, shadowRadius: 6, elevation: 4,
          }}>
            <Text style={{ fontSize: 22, fontWeight: "800", color: "#2D7A4F", lineHeight: 24 }}>24</Text>
            <Text style={{ fontSize: 9, color: "#2D7A4F", fontWeight: "600" }}>Weeks</Text>
          </View>
          <Text style={{ fontSize: 12, color: "#888", marginTop: 8 }}>
            2nd Trimester • Week 24
          </Text>
        </View>

        {/* Title */}
        <View style={{ paddingHorizontal: 24, marginTop: 16, marginBottom: 16 }}>
         <View style={{ flexDirection: "row", alignItems: "center", gap: 8, marginBottom: 6 }}>
              <Text style={{ fontSize: 26, fontWeight: "800", color: "#111" }}>
                Welcome to MamaCare
              </Text>
              <Ionicons name="heart" size={22} color="#2D7A4F" />
            </View>
          <Text style={{ fontSize: 14, color: "#777", lineHeight: 21 }}>
            Your trusted pregnancy companion, designed for Nigerian Mamas.
          </Text>
        </View>

        {/* Features card */}
        <View style={{
          marginHorizontal: 24,
          backgroundColor: "#F0FAF4",
          borderRadius: 16,
          padding: 18,
          gap: 14,
        }}>
          <Text style={{ fontSize: 13, fontWeight: "700", color: "#333", marginBottom: 4 }}>
            What you will get:
          </Text>
          {FEATURES.map((f) => (
            <View key={f.label} style={{ flexDirection: "row", alignItems: "center", gap: 14 }}>
              <View style={{
                width: 36, height: 36, borderRadius: 10,
                backgroundColor: "#fff", alignItems: "center", justifyContent: "center",
                shadowColor: "#000", shadowOffset: { width: 0, height: 1 },
                shadowOpacity: 0.06, shadowRadius: 4, elevation: 2,
              }}>
                <Ionicons name={f.icon as any} size={18} color="#2D7A4F" />
              </View>
              <Text style={{ fontSize: 14, color: "#333", fontWeight: "500" }}>{f.label}</Text>
            </View>
          ))}
        </View>

        {/* Continue button */}
        <View style={{ paddingHorizontal: 24, marginTop: 28 }}>
          <TouchableOpacity
            onPress={() => router.push("/(onboarding)/know-you")}
            style={{
              backgroundColor: "#2D7A4F",
              borderRadius: 14,
              paddingVertical: 17,
              flexDirection: "row",
              alignItems: "center",
              justifyContent: "center",
              gap: 8,
              shadowColor: "#2D7A4F",
              shadowOffset: { width: 0, height: 6 },
              shadowOpacity: 0.3,
              shadowRadius: 12,
              elevation: 6,
            }}
          >
            <Text style={{ color: "#fff", fontWeight: "700", fontSize: 16 }}>Continue</Text>
            <Ionicons name="arrow-forward" size={18} color="#fff" />
          </TouchableOpacity>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}
