import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  Image,
  TouchableOpacity,
  ScrollView,
  ActivityIndicator,
  Dimensions,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import * as SecureStore from "expo-secure-store";

const { width } = Dimensions.get("window");
const BASE_URL = process.env.EXPO_PUBLIC_API_URL;

const CATEGORIES = ["All", "Nutrition", "Exercise", "Mental Health", "Baby Care", "Delivery"];

const TIPS = [
  {
    icon: "restaurant-outline",
    title: "Nutrition",
    desc: "Eat healthy for you and your baby.",
  },
  {
    icon: "water-outline",
    title: "Stay Hydrated",
    desc: "Drink enough water daily.",
  },
  {
    icon: "moon-outline",
    title: "Get Enough Rest",
    desc: "Good rest supports a healthy pregnancy.",
  },
  {
    icon: "happy-outline",
    title: "Manage Stress",
    desc: "Stay calm and think positive.",
  },
];

// Fallback learn items if API has no content yet
const FALLBACK_ITEMS = [
  { title: "Nutrition for you and baby", duration: "2.30 min", category: "Nutrition" },
  { title: "Managing stress the healthy way", duration: "1.45 min", category: "Mental Health" },
  { title: "Danger signs in pregnancy", duration: "1.30 min", category: "Baby Care" },
  { title: "Preparing for your baby's delivery", duration: "1.35 min", category: "Delivery" },
  { title: "Safe exercises during pregnancy", duration: "2.00 min", category: "Exercise" },
  { title: "Understanding your baby's movements", duration: "1.15 min", category: "Baby Care" },
];

export default function LearnScreen() {
  const [selectedCategory, setSelectedCategory] = useState("All");
  const [loading, setLoading] = useState(false);
  const [learnItems, setLearnItems] = useState(FALLBACK_ITEMS);

  const filtered = selectedCategory === "All"
    ? learnItems
    : learnItems.filter((i) => i.category === selectedCategory);

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#F8F8F8" }}>
      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 32 }}>

        {/* Header */}
        <View style={{
          flexDirection: "row", alignItems: "center", gap: 10,
          paddingHorizontal: 20, paddingTop: 16, paddingBottom: 14,
          backgroundColor: "#fff",
        }}>
          <TouchableOpacity onPress={() => router.back()} style={{ width: 36, height: 36, justifyContent: "center" }}>
            <Ionicons name="arrow-back" size={24} color="#111" />
          </TouchableOpacity>
          <Text style={{ fontSize: 18, fontWeight: "800", color: "#111" }}>Health Tips</Text>
        </View>

        {/* Hero banner */}
        <View style={{
          marginHorizontal: 20, marginTop: 16, marginBottom: 16,
          backgroundColor: "#F0FAF4", borderRadius: 20, padding: 20,
          flexDirection: "row", alignItems: "center",
        }}>
          <View style={{ flex: 1 }}>
            <Text style={{ fontSize: 20, fontWeight: "800", color: "#2D7A4F", lineHeight: 28, marginBottom: 12 }}>
              Small steps,{"\n"}healthy you,{"\n"}healthy baby.
            </Text>
            <TouchableOpacity style={{
              flexDirection: "row", alignItems: "center", gap: 8,
              backgroundColor: "#fff", borderRadius: 25,
              paddingHorizontal: 14, paddingVertical: 8,
              alignSelf: "flex-start",
              shadowColor: "#000", shadowOffset: { width: 0, height: 2 },
              shadowOpacity: 0.06, shadowRadius: 6, elevation: 2,
            }}>
              <Ionicons name="volume-medium-outline" size={16} color="#2D7A4F" />
              <Text style={{ fontSize: 12, color: "#2D7A4F", fontWeight: "600" }}>
                Listen in Yoruba
              </Text>
              <View style={{
                width: 24, height: 24, borderRadius: 12,
                backgroundColor: "#2D7A4F", alignItems: "center", justifyContent: "center",
              }}>
                <Ionicons name="play" size={10} color="#fff" />
              </View>
            </TouchableOpacity>
          </View>
          <Image
            source={require("../../assets/images/10.png")}
            style={{ width: 110, height: 130 }}
            resizeMode="contain"
          />
        </View>

        {/* Category pills */}
        <ScrollView
          horizontal
          showsHorizontalScrollIndicator={false}
          contentContainerStyle={{ gap: 8, paddingHorizontal: 20, marginBottom: 16 }}
        >
          {CATEGORIES.map((cat) => (
            <TouchableOpacity
              key={cat}
              onPress={() => setSelectedCategory(cat)}
              style={{
                paddingHorizontal: 16, paddingVertical: 8, borderRadius: 20,
                backgroundColor: selectedCategory === cat ? "#2D7A4F" : "#fff",
                borderWidth: 1,
                borderColor: selectedCategory === cat ? "#2D7A4F" : "#E8E8E8",
              }}
            >
              <Text style={{
                fontSize: 13, fontWeight: "600",
                color: selectedCategory === cat ? "#fff" : "#555",
              }}>
                {cat}
              </Text>
            </TouchableOpacity>
          ))}
        </ScrollView>

        {/* Tips section */}
        <View style={{ paddingHorizontal: 20, marginBottom: 20 }}>
          <Text style={{ fontSize: 15, fontWeight: "700", color: "#111", marginBottom: 12 }}>Tips</Text>
          <View style={{ gap: 10 }}>
            {TIPS.map((tip) => (
              <TouchableOpacity
                key={tip.title}
                style={{
                  flexDirection: "row", alignItems: "center", gap: 14,
                  backgroundColor: "#fff", borderRadius: 14, padding: 14,
                  shadowColor: "#000", shadowOffset: { width: 0, height: 1 },
                  shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
                }}
              >
                <View style={{
                  width: 40, height: 40, borderRadius: 12,
                  backgroundColor: "#F0FAF4", alignItems: "center", justifyContent: "center",
                }}>
                  <Ionicons name={tip.icon as any} size={20} color="#2D7A4F" />
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={{ fontSize: 14, fontWeight: "700", color: "#111", marginBottom: 2 }}>
                    {tip.title}
                  </Text>
                  <Text style={{ fontSize: 12, color: "#888" }}>{tip.desc}</Text>
                </View>
                <Ionicons name="chevron-forward" size={16} color="#CCC" />
              </TouchableOpacity>
            ))}
          </View>
        </View>

        {/* Tip of the Day */}
        <View style={{
          marginHorizontal: 20, marginBottom: 20,
          backgroundColor: "#F0FAF4", borderRadius: 16, padding: 16,
          borderLeftWidth: 4, borderLeftColor: "#2D7A4F",
        }}>
          <View style={{ flexDirection: "row", alignItems: "center", gap: 8, marginBottom: 8 }}>
            <Ionicons name="bulb-outline" size={18} color="#2D7A4F" />
            <Text style={{ fontSize: 13, fontWeight: "700", color: "#2D7A4F" }}>Tips of the Day</Text>
          </View>
          <Text style={{ fontSize: 13, color: "#555", lineHeight: 20 }}>
            Eat a variety of fruits and vegetables everyday.
          </Text>
        </View>

        {/* Learn & Listen list */}
        <View style={{ paddingHorizontal: 20 }}>
          <Text style={{ fontSize: 15, fontWeight: "700", color: "#111", marginBottom: 12 }}>
            Learn & Listen
          </Text>
          {loading ? (
            <ActivityIndicator color="#2D7A4F" />
          ) : (
            <View style={{ gap: 10 }}>
              {filtered.map((item, i) => (
                <TouchableOpacity
                  key={i}
                  style={{
                    flexDirection: "row", alignItems: "center", gap: 12,
                    backgroundColor: "#fff", borderRadius: 14, padding: 12,
                    shadowColor: "#000", shadowOffset: { width: 0, height: 1 },
                    shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
                  }}
                >
                  <View style={{
                    width: 56, height: 56, borderRadius: 12,
                    backgroundColor: "#E8F5EE", alignItems: "center", justifyContent: "center",
                    overflow: "hidden",
                  }}>
                    <Image
                      source={require("../../assets/images/10.png")}
                      style={{ width: 56, height: 56 }}
                      resizeMode="cover"
                    />
                  </View>
                  <View style={{ flex: 1 }}>
                    <Text style={{ fontSize: 13, fontWeight: "700", color: "#111", marginBottom: 4, lineHeight: 18 }}>
                      {item.title}
                    </Text>
                    <View style={{ flexDirection: "row", alignItems: "center", gap: 6 }}>
                      <Ionicons name="time-outline" size={12} color="#AAA" />
                      <Text style={{ fontSize: 11, color: "#AAA" }}>{item.duration}</Text>
                      <View style={{
                        backgroundColor: "#E8F5EE", borderRadius: 10,
                        paddingHorizontal: 8, paddingVertical: 2, marginLeft: 4,
                      }}>
                        <Text style={{ fontSize: 10, color: "#2D7A4F", fontWeight: "600" }}>
                          {item.category}
                        </Text>
                      </View>
                    </View>
                  </View>
                  <TouchableOpacity style={{
                    width: 32, height: 32, borderRadius: 16,
                    backgroundColor: "#2D7A4F", alignItems: "center", justifyContent: "center",
                  }}>
                    <Ionicons name="play" size={13} color="#fff" />
                  </TouchableOpacity>
                </TouchableOpacity>
              ))}
            </View>
          )}
        </View>

      </ScrollView>
    </SafeAreaView>
  );
}
