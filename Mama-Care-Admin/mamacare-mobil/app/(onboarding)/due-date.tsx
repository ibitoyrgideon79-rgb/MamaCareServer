import React, { useState } from "react";
import {
  View, Text, Image, TouchableOpacity,
  ScrollView, TextInput, Modal, Dimensions,
} from "react-native";
import { router, useLocalSearchParams } from "expo-router";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import * as SecureStore from "expo-secure-store";

const { width } = Dimensions.get("window");
const BASE_URL = process.env.EXPO_PUBLIC_API_URL;

export default function DueDateScreen() {
  const { mode } = useLocalSearchParams<{ mode: string }>();
  const isLMP = mode === "lmp";

  const [date, setDate] = useState("");
  const [error, setError] = useState("");
  const [showPicker, setShowPicker] = useState(false);
  const [loading, setLoading] = useState(false);

  const [pickerMonth, setPickerMonth] = useState(new Date().getMonth());
  const [pickerYear, setPickerYear] = useState(new Date().getFullYear());
  const [pickerDay, setPickerDay] = useState(new Date().getDate());

  const months = [
    "January","February","March","April","May","June",
    "July","August","September","October","November","December",
  ];

  const getDaysInMonth = (month: number, year: number) =>
    new Date(year, month + 1, 0).getDate();

  const handleDateInput = (text: string) => {
    const cleaned = text.replace(/\D/g, "");
    let formatted = cleaned;
    if (cleaned.length >= 3) formatted = cleaned.slice(0, 2) + "/" + cleaned.slice(2);
    if (cleaned.length >= 5) formatted = cleaned.slice(0, 2) + "/" + cleaned.slice(2, 4) + "/" + cleaned.slice(4, 8);
    setDate(formatted);
    setError("");
  };

  const confirmPickerDate = () => {
    const d = String(pickerDay).padStart(2, "0");
    const m = String(pickerMonth + 1).padStart(2, "0");
    setDate(`${m}/${d}/${pickerYear}`);
    setShowPicker(false);
    setError("");
  };

  // Convert mm/dd/yyyy to yyyy-MM-dd for API
  const toISODate = (mmddyyyy: string) => {
    const parts = mmddyyyy.split("/");
    if (parts.length !== 3) return null;
    return `${parts[2]}-${parts[0]}-${parts[1]}`;
  };

  const validate = () => {
    if (!date.trim()) {
      setError(isLMP ? "Please enter your last menstrual period date." : "Please enter your expected due date.");
      return false;
    }
    const parts = date.split("/");
    if (parts.length !== 3 || parts[2].length !== 4) {
      setError("Please enter a valid date in mm/dd/yyyy format.");
      return false;
    }
    return true;
  };

  const handleStart = async () => {
    if (!validate()) return;
    setLoading(true);
    try {
      const token = await SecureStore.getItemAsync("accessToken");
      const isoDate = toISODate(date);

      // Build pregnancy setup payload
      const payload: any = {};
      if (isLMP) {
        payload.lastMenstrualPeriodDate = isoDate;
      } else {
        payload.dueDate = isoDate;
      }

      const response = await fetch(`${BASE_URL}/api/v1/pregnancy/setup`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (response.ok || response.status === 200) {
        const data = await response.json();
        // Save pregnancy info locally
        await SecureStore.setItemAsync("dateMode", mode || "due_date");
        await SecureStore.setItemAsync("dateValue", date);
        await SecureStore.setItemAsync("pregnancyWeeks", String(data.weeksOfPregnancy || 0));
        await SecureStore.setItemAsync("dueDate", data.dueDate || "");
      } else {
        // Save locally even if API fails — will sync later
        await SecureStore.setItemAsync("dateMode", mode || "due_date");
        await SecureStore.setItemAsync("dateValue", date);
      }
    } catch (e) {
      // Save locally if no connection
      await SecureStore.setItemAsync("dateMode", mode || "due_date");
      await SecureStore.setItemAsync("dateValue", date);
    } finally {
      setLoading(false);
      router.replace("/(tabs)/home");
    }
  };

  const days = Array.from({ length: getDaysInMonth(pickerMonth, pickerYear) }, (_, i) => i + 1);
  const years = Array.from({ length: 3 }, (_, i) => new Date().getFullYear() - 1 + i);

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#fff" }}>
      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={{ paddingBottom: 36 }}
        keyboardShouldPersistTaps="handled"
      >
        {/* Header */}
        <View style={{
          flexDirection: "row", alignItems: "center",
          justifyContent: "space-between",
          paddingHorizontal: 20, paddingTop: 16,
        }}>
          <TouchableOpacity onPress={() => router.back()} style={{ width: 38, height: 38, justifyContent: "center" }}>
            <Ionicons name="arrow-back" size={24} color="#111" />
          </TouchableOpacity>
          <TouchableOpacity onPress={() => router.replace("/(tabs)/home")}>
            <Text style={{ color: "#2D7A4F", fontSize: 15, fontWeight: "600" }}>Skip</Text>
          </TouchableOpacity>
        </View>

        {/* Baby image */}
        <View style={{ alignItems: "center", marginTop: 10 }}>
          <View style={{
            width: 110, height: 110, borderRadius: 55,
            backgroundColor: "#F0FAF4", alignItems: "center", justifyContent: "center",
          }}>
            <Image
              source={require("../../assets/images/8.png")}
              style={{ width: 80, height: 80 }}
              resizeMode="contain"
            />
          </View>
        </View>

        {/* Page dots */}
        <View style={{ flexDirection: "row", justifyContent: "center", gap: 6, marginTop: 16 }}>
          {[0, 1, 2].map((i) => (
            <View key={i} style={{
              width: i === 2 ? 20 : 8, height: 8, borderRadius: 4,
              backgroundColor: i === 2 ? "#2D7A4F" : "#D5D5D5",
            }} />
          ))}
        </View>

        {/* Title */}
        <View style={{ paddingHorizontal: 24, marginTop: 20, marginBottom: 28 }}>
          <Text style={{ fontSize: 26, fontWeight: "800", color: "#111", marginBottom: 8 }}>
            {isLMP ? "When did your period start?" : "When is baby coming?"}
          </Text>
          <Text style={{ fontSize: 14, color: "#888", lineHeight: 21 }}>
            {isLMP
              ? "We'll use this to calculate your due date and track your pregnancy week by week."
              : "This helps us to track your pregnancy week by week."}
          </Text>
        </View>

        {/* Date input */}
        <View style={{ paddingHorizontal: 24 }}>
          <Text style={{ fontSize: 13, fontWeight: "600", color: "#444", marginBottom: 10 }}>
            {isLMP ? "Last Menstrual Period Date" : "Expected Due Date"}
          </Text>
          <TouchableOpacity
            onPress={() => setShowPicker(true)}
            style={{
              flexDirection: "row", alignItems: "center",
              borderWidth: 1.5,
              borderColor: error ? "#E53935" : "#EFEFEF",
              borderRadius: 12, backgroundColor: "#FAFAFA",
              paddingHorizontal: 16, height: 54,
              justifyContent: "space-between",
            }}
          >
            <TextInput
              placeholder="mm/dd/yyyy"
              placeholderTextColor="#BDBDBD"
              value={date}
              onChangeText={handleDateInput}
              keyboardType="numeric"
              maxLength={10}
              style={{ flex: 1, fontSize: 15, color: "#333" }}
            />
            <Ionicons name="calendar-outline" size={20} color="#2D7A4F" />
          </TouchableOpacity>
          {error ? (
            <Text style={{ color: "#E53935", fontSize: 12, marginTop: 6, marginLeft: 2 }}>
              {error}
            </Text>
          ) : null}
        </View>

        {/* Buttons */}
        <View style={{ paddingHorizontal: 24, marginTop: 40, gap: 12 }}>
          <TouchableOpacity
            onPress={handleStart}
            disabled={loading}
            style={{
              backgroundColor: loading ? "#7AAF90" : "#2D7A4F",
              borderRadius: 14, paddingVertical: 17,
              flexDirection: "row", alignItems: "center", justifyContent: "center", gap: 8,
              shadowColor: "#2D7A4F", shadowOffset: { width: 0, height: 6 },
              shadowOpacity: 0.3, shadowRadius: 12, elevation: 6,
            }}
          >
            <Text style={{ color: "#fff", fontWeight: "700", fontSize: 16 }}>
              {loading ? "Saving..." : "Start My Journey"}
            </Text>
            {!loading && <Ionicons name="heart-outline" size={18} color="#fff" />}
          </TouchableOpacity>

          <TouchableOpacity
            onPress={() => router.replace("/(tabs)/home")}
            style={{
              borderRadius: 14, paddingVertical: 17, alignItems: "center",
              borderWidth: 1.5, borderColor: "#2D7A4F",
            }}
          >
            <Text style={{ color: "#2D7A4F", fontWeight: "600", fontSize: 16 }}>
              I'll do this later
            </Text>
          </TouchableOpacity>
        </View>
      </ScrollView>

      {/* Date Picker Modal */}
      <Modal visible={showPicker} transparent animationType="slide">
        <View style={{ flex: 1, backgroundColor: "rgba(0,0,0,0.4)", justifyContent: "flex-end" }}>
          <View style={{
            backgroundColor: "#fff", borderTopLeftRadius: 24,
            borderTopRightRadius: 24, padding: 24,
          }}>
            <Text style={{ fontSize: 16, fontWeight: "700", color: "#111", marginBottom: 20, textAlign: "center" }}>
              Select Date
            </Text>

            <Text style={{ fontSize: 12, fontWeight: "600", color: "#888", marginBottom: 8 }}>Month</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: 16 }}>
              <View style={{ flexDirection: "row", gap: 8 }}>
                {months.map((m, i) => (
                  <TouchableOpacity
                    key={m}
                    onPress={() => setPickerMonth(i)}
                    style={{
                      paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
                      backgroundColor: pickerMonth === i ? "#2D7A4F" : "#F5F5F5",
                    }}
                  >
                    <Text style={{ color: pickerMonth === i ? "#fff" : "#555", fontWeight: "600", fontSize: 13 }}>
                      {m}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>
            </ScrollView>

            <Text style={{ fontSize: 12, fontWeight: "600", color: "#888", marginBottom: 8 }}>Day</Text>
            <ScrollView horizontal showsHorizontalScrollIndicator={false} style={{ marginBottom: 16 }}>
              <View style={{ flexDirection: "row", gap: 8 }}>
                {days.map((d) => (
                  <TouchableOpacity
                    key={d}
                    onPress={() => setPickerDay(d)}
                    style={{
                      width: 40, height: 40, borderRadius: 20,
                      backgroundColor: pickerDay === d ? "#2D7A4F" : "#F5F5F5",
                      alignItems: "center", justifyContent: "center",
                    }}
                  >
                    <Text style={{ color: pickerDay === d ? "#fff" : "#555", fontWeight: "600" }}>{d}</Text>
                  </TouchableOpacity>
                ))}
              </View>
            </ScrollView>

            <Text style={{ fontSize: 12, fontWeight: "600", color: "#888", marginBottom: 8 }}>Year</Text>
            <View style={{ flexDirection: "row", gap: 8, marginBottom: 24 }}>
              {years.map((y) => (
                <TouchableOpacity
                  key={y}
                  onPress={() => setPickerYear(y)}
                  style={{
                    flex: 1, paddingVertical: 10, borderRadius: 12,
                    backgroundColor: pickerYear === y ? "#2D7A4F" : "#F5F5F5",
                    alignItems: "center",
                  }}
                >
                  <Text style={{ color: pickerYear === y ? "#fff" : "#555", fontWeight: "600" }}>{y}</Text>
                </TouchableOpacity>
              ))}
            </View>

            <TouchableOpacity
              onPress={confirmPickerDate}
              style={{ backgroundColor: "#2D7A4F", borderRadius: 14, paddingVertical: 15, alignItems: "center" }}
            >
              <Text style={{ color: "#fff", fontWeight: "700", fontSize: 15 }}>Confirm</Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={() => setShowPicker(false)}
              style={{ paddingVertical: 12, alignItems: "center", marginTop: 4 }}
            >
              <Text style={{ color: "#888", fontSize: 14 }}>Cancel</Text>
            </TouchableOpacity>
          </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}
