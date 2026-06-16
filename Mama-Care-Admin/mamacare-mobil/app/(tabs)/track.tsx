import React, { useState, useEffect } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  ScrollView,
  Switch,
  ActivityIndicator,
  Modal,
  TextInput,
  Alert,
} from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { router } from "expo-router";
import * as SecureStore from "expo-secure-store";

const BASE_URL = process.env.EXPO_PUBLIC_API_URL;

async function authHeaders() {
  const token = await SecureStore.getItemAsync("accessToken");
  return {
    "Content-Type": "application/json",
    "Authorization": `Bearer ${token}`,
  };
}

const APPOINTMENT_TYPES = ["CHECKUP", "ULTRASOUND", "BLOOD_TEST", "DELIVERY", "FOLLOW_UP"];
const REMINDER_OFFSETS = ["PT_5_MINUTES", "PT_15_MINUTES", "PT_30_MINUTES", "PT_1_HOUR", "PT_1_DAY", "PT_3_DAYS"];
const OFFSET_LABELS: Record<string, string> = {
  PT_5_MINUTES: "5 mins before",
  PT_15_MINUTES: "15 mins before",
  PT_30_MINUTES: "30 mins before",
  PT_1_HOUR: "1 hour before",
  PT_1_DAY: "1 day before",
  PT_3_DAYS: "3 days before",
};

export default function TrackScreen() {
  const [activeTab, setActiveTab] = useState<"upcoming" | "completed">("upcoming");
  const [alertsEnabled, setAlertsEnabled] = useState(true);
  const [loading, setLoading] = useState(true);
  const [appointments, setAppointments] = useState<any[]>([]);
  const [medications, setMedications] = useState<any[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [addType, setAddType] = useState<"appointment" | "medication">("appointment");

  // Add appointment form
  const [apptForm, setApptForm] = useState({
    title: "",
    description: "",
    appointmentDate: "",
    appointmentType: "CHECKUP",
    reminderOffset: "PT_1_DAY",
  });

  // Add medication form
  const [medForm, setMedForm] = useState({
    medicationName: "",
    dosage: "",
    frequency: "DAILY",
    quantity: "",
    reminderTime: "",
    notes: "",
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    setLoading(true);
    try {
      const headers = await authHeaders();
      const [apptRes, medRes] = await Promise.all([
        fetch(`${BASE_URL}/api/v1/appointments/upcoming/next`, { headers }),
        fetch(`${BASE_URL}/api/v1/medications/today`, { headers }),
      ]);

      if (apptRes.ok) {
        const data = await apptRes.json();
        setAppointments(data ? [data] : []);
      }
      if (medRes.ok) {
        const data = await medRes.json();
        setMedications(Array.isArray(data) ? data : []);
      }
    } catch (e) {
      // No connection — show empty state
    } finally {
      setLoading(false);
    }
  };

  const handleAddAppointment = async () => {
    if (!apptForm.title || !apptForm.appointmentDate) {
      Alert.alert("Missing fields", "Please fill in title and date.");
      return;
    }
    try {
      const headers = await authHeaders();
      const response = await fetch(`${BASE_URL}/api/v1/appointments/`, {
        method: "POST",
        headers,
        body: JSON.stringify({
          title: apptForm.title,
          description: apptForm.description,
          appointmentDate: apptForm.appointmentDate,
          appointmentType: apptForm.appointmentType,
          reminderOffsets: [apptForm.reminderOffset],
          checklistItems: [],
        }),
      });
      if (response.ok || response.status === 201) {
        setShowAddModal(false);
        setApptForm({ title: "", description: "", appointmentDate: "", appointmentType: "CHECKUP", reminderOffset: "PT_1_DAY" });
        loadData();
      }
    } catch (e) {}
  };

  const handleAddMedication = async () => {
    if (!medForm.medicationName || !medForm.reminderTime) {
      Alert.alert("Missing fields", "Please fill in medication name and reminder time.");
      return;
    }
    try {
      const headers = await authHeaders();
      const response = await fetch(`${BASE_URL}/api/v1/medications/`, {
        method: "POST",
        headers,
        body: JSON.stringify({
          medicationName: medForm.medicationName,
          dosage: medForm.dosage,
          frequency: medForm.frequency,
          dosageUnit: "tablet",
          quantity: parseInt(medForm.quantity) || 30,
          prescribedDate: new Date().toISOString().split("T")[0],
          reminderTime: medForm.reminderTime + ":00",
          notes: medForm.notes,
        }),
      });
      if (response.ok || response.status === 201) {
        setShowAddModal(false);
        setMedForm({ medicationName: "", dosage: "", frequency: "DAILY", quantity: "", reminderTime: "", notes: "" });
        loadData();
      }
    } catch (e) {}
  };

  const handleMarkMedTaken = async (medId: number) => {
    try {
      const headers = await authHeaders();
      await fetch(`${BASE_URL}/api/v1/medications/${medId}/taken`, {
        method: "PATCH",
        headers,
      });
      loadData();
    } catch (e) {}
  };

  const allReminders = [
    ...appointments.map((a) => ({
      id: a.id,
      type: "appointment",
      icon: "calendar-outline",
      title: a.title,
      date: a.appointmentDate
        ? new Date(a.appointmentDate).toLocaleDateString("en-NG", {
            month: "short", day: "numeric", year: "numeric", hour: "2-digit", minute: "2-digit",
          })
        : "",
      sub: a.daysUntilAppointment != null ? `In ${a.daysUntilAppointment} days` : "",
      completed: a.status === "COMPLETED",
      color: "#F0FAF4",
    })),
    ...medications.map((m) => ({
      id: m.medicationId || m.id,
      type: "medication",
      icon: "medical-outline",
      title: m.medicationName,
      date: m.reminderTime?.slice(0, 5) || "",
      sub: "Daily reminder",
      completed: m.isTaken || false,
      color: "#FFF8F0",
    })),
    {
      id: "walk",
      type: "walk",
      icon: "walk-outline",
      title: "Evening Walk",
      date: "Today 6:00 PM",
      sub: "Lets move gently",
      completed: false,
      color: "#F0F4FF",
    },
  ];

  const filtered = activeTab === "upcoming"
    ? allReminders.filter((r) => !r.completed)
    : allReminders.filter((r) => r.completed);

  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: "#F8F8F8" }}>

      {/* Add Reminder Modal */}
      <Modal visible={showAddModal} transparent animationType="slide">
        <View style={{ flex: 1, backgroundColor: "rgba(0,0,0,0.4)", justifyContent: "flex-end" }}>
          <View style={{
            backgroundColor: "#fff", borderTopLeftRadius: 24,
            borderTopRightRadius: 24, padding: 24, maxHeight: "90%",
          }}>
            <ScrollView showsVerticalScrollIndicator={false}>
              {/* Modal header */}
              <View style={{ flexDirection: "row", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
                <Text style={{ fontSize: 18, fontWeight: "800", color: "#111" }}>Add Reminder</Text>
                <TouchableOpacity onPress={() => setShowAddModal(false)}>
                  <Ionicons name="close" size={24} color="#555" />
                </TouchableOpacity>
              </View>

              {/* Type toggle */}
              <View style={{ flexDirection: "row", gap: 10, marginBottom: 20 }}>
                {(["appointment", "medication"] as const).map((t) => (
                  <TouchableOpacity
                    key={t}
                    onPress={() => setAddType(t)}
                    style={{
                      flex: 1, paddingVertical: 10, borderRadius: 10, alignItems: "center",
                      backgroundColor: addType === t ? "#2D7A4F" : "#F5F5F5",
                    }}
                  >
                    <Text style={{ color: addType === t ? "#fff" : "#555", fontWeight: "700", fontSize: 13 }}>
                      {t === "appointment" ? "Appointment" : "Medication"}
                    </Text>
                  </TouchableOpacity>
                ))}
              </View>

              {addType === "appointment" ? (
                <View style={{ gap: 12 }}>
                  <FormField placeholder="Title (e.g. Doctor's Checkup)" value={apptForm.title} onChangeText={(v) => setApptForm({ ...apptForm, title: v })} />
                  <FormField placeholder="Description" value={apptForm.description} onChangeText={(v) => setApptForm({ ...apptForm, description: v })} />
                  <FormField placeholder="Date & Time (e.g. 2025-06-15T10:00:00)" value={apptForm.appointmentDate} onChangeText={(v) => setApptForm({ ...apptForm, appointmentDate: v })} />

                  <Text style={{ fontSize: 12, fontWeight: "600", color: "#444", marginBottom: 4 }}>Appointment Type</Text>
                  <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8 }}>
                    {APPOINTMENT_TYPES.map((t) => (
                      <TouchableOpacity
                        key={t}
                        onPress={() => setApptForm({ ...apptForm, appointmentType: t })}
                        style={{
                          paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
                          backgroundColor: apptForm.appointmentType === t ? "#2D7A4F" : "#F5F5F5",
                        }}
                      >
                        <Text style={{ fontSize: 12, fontWeight: "600", color: apptForm.appointmentType === t ? "#fff" : "#555" }}>
                          {t.replace("_", " ")}
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </ScrollView>

                  <Text style={{ fontSize: 12, fontWeight: "600", color: "#444", marginBottom: 4, marginTop: 8 }}>Reminder</Text>
                  <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={{ gap: 8 }}>
                    {REMINDER_OFFSETS.map((r) => (
                      <TouchableOpacity
                        key={r}
                        onPress={() => setApptForm({ ...apptForm, reminderOffset: r })}
                        style={{
                          paddingHorizontal: 14, paddingVertical: 8, borderRadius: 20,
                          backgroundColor: apptForm.reminderOffset === r ? "#2D7A4F" : "#F5F5F5",
                        }}
                      >
                        <Text style={{ fontSize: 12, fontWeight: "600", color: apptForm.reminderOffset === r ? "#fff" : "#555" }}>
                          {OFFSET_LABELS[r]}
                        </Text>
                      </TouchableOpacity>
                    ))}
                  </ScrollView>

                  <TouchableOpacity
                    onPress={handleAddAppointment}
                    style={{
                      backgroundColor: "#2D7A4F", borderRadius: 14,
                      paddingVertical: 15, alignItems: "center", marginTop: 12,
                    }}
                  >
                    <Text style={{ color: "#fff", fontWeight: "700", fontSize: 15 }}>Save Appointment</Text>
                  </TouchableOpacity>
                </View>
              ) : (
                <View style={{ gap: 12 }}>
                  <FormField placeholder="Medication name (e.g. Folic Acid)" value={medForm.medicationName} onChangeText={(v) => setMedForm({ ...medForm, medicationName: v })} />
                  <FormField placeholder="Dosage (e.g. 1 tablet)" value={medForm.dosage} onChangeText={(v) => setMedForm({ ...medForm, dosage: v })} />
                  <FormField placeholder="Quantity (e.g. 30)" value={medForm.quantity} onChangeText={(v) => setMedForm({ ...medForm, quantity: v })} keyboardType="numeric" />
                  <FormField placeholder="Reminder time (e.g. 08:00)" value={medForm.reminderTime} onChangeText={(v) => setMedForm({ ...medForm, reminderTime: v })} />
                  <FormField placeholder="Notes (optional)" value={medForm.notes} onChangeText={(v) => setMedForm({ ...medForm, notes: v })} />

                  <TouchableOpacity
                    onPress={handleAddMedication}
                    style={{
                      backgroundColor: "#2D7A4F", borderRadius: 14,
                      paddingVertical: 15, alignItems: "center", marginTop: 12,
                    }}
                  >
                    <Text style={{ color: "#fff", fontWeight: "700", fontSize: 15 }}>Save Medication</Text>
                  </TouchableOpacity>
                </View>
              )}
            </ScrollView>
          </View>
        </View>
      </Modal>

      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={{ paddingBottom: 32 }}>

        {/* Header */}
        <View style={{
          flexDirection: "row", alignItems: "center", justifyContent: "space-between",
          paddingHorizontal: 20, paddingTop: 16, paddingBottom: 14, backgroundColor: "#fff",
        }}>
          <View style={{ flexDirection: "row", alignItems: "center", gap: 10 }}>
            <TouchableOpacity onPress={() => router.back()} style={{ width: 36, height: 36, justifyContent: "center" }}>
              <Ionicons name="arrow-back" size={24} color="#111" />
            </TouchableOpacity>
            <Text style={{ fontSize: 18, fontWeight: "800", color: "#111" }}>Reminders</Text>
          </View>
        </View>

        {/* Alert toggle */}
        <View style={{
          flexDirection: "row", alignItems: "center", justifyContent: "space-between",
          marginHorizontal: 20, marginTop: 16, marginBottom: 16,
          backgroundColor: "#fff", borderRadius: 14, padding: 16,
          shadowColor: "#000", shadowOffset: { width: 0, height: 1 },
          shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
        }}>
          <View style={{ flexDirection: "row", alignItems: "center", gap: 12 }}>
            <Ionicons name="notifications-outline" size={22} color="#888" />
            <View>
              <Text style={{ fontSize: 14, fontWeight: "700", color: "#111" }}>Reminder Alerts</Text>
              <Text style={{ fontSize: 12, color: "#888" }}>Get reminded about important things.</Text>
            </View>
          </View>
          <Switch
            value={alertsEnabled}
            onValueChange={setAlertsEnabled}
            trackColor={{ false: "#E0E0E0", true: "#2D7A4F" }}
            thumbColor="#fff"
          />
        </View>

        {/* Tabs */}
        <View style={{
          flexDirection: "row", marginHorizontal: 20, marginBottom: 16,
          backgroundColor: "#F5F5F5", borderRadius: 12, padding: 4,
        }}>
          {(["upcoming", "completed"] as const).map((tab) => (
            <TouchableOpacity
              key={tab}
              onPress={() => setActiveTab(tab)}
              style={{
                flex: 1, paddingVertical: 10, borderRadius: 10, alignItems: "center",
                backgroundColor: activeTab === tab ? "#fff" : "transparent",
                shadowColor: activeTab === tab ? "#000" : "transparent",
                shadowOffset: { width: 0, height: 1 },
                shadowOpacity: 0.08, shadowRadius: 4, elevation: activeTab === tab ? 2 : 0,
              }}
            >
              <Text style={{
                fontSize: 13, fontWeight: "700",
                color: activeTab === tab ? "#2D7A4F" : "#888",
              }}>
                {tab.charAt(0).toUpperCase() + tab.slice(1)}
              </Text>
            </TouchableOpacity>
          ))}
        </View>

        {/* Reminder list */}
        <View style={{ paddingHorizontal: 20, gap: 10 }}>
          {loading ? (
            <ActivityIndicator color="#2D7A4F" style={{ marginTop: 20 }} />
          ) : filtered.length === 0 ? (
            <View style={{ alignItems: "center", marginTop: 40 }}>
              <Ionicons name="calendar-outline" size={48} color="#CCC" />
              <Text style={{ fontSize: 15, color: "#AAA", marginTop: 12, fontWeight: "600" }}>
                No {activeTab} reminders
              </Text>
            </View>
          ) : (
            filtered.map((r: any, i) => (
              <TouchableOpacity
                key={i}
                style={{
                  flexDirection: "row", alignItems: "center", gap: 14,
                  backgroundColor: "#fff", borderRadius: 14, padding: 14,
                  shadowColor: "#000", shadowOffset: { width: 0, height: 1 },
                  shadowOpacity: 0.05, shadowRadius: 6, elevation: 1,
                }}
              >
                <View style={{
                  width: 42, height: 42, borderRadius: 12,
                  backgroundColor: r.color, alignItems: "center", justifyContent: "center",
                }}>
                  <Ionicons name={r.icon as any} size={20} color="#2D7A4F" />
                </View>
                <View style={{ flex: 1 }}>
                  <Text style={{ fontSize: 14, fontWeight: "700", color: "#111", marginBottom: 3 }}>
                    {r.title}
                  </Text>
                  <Text style={{ fontSize: 12, color: "#888" }}>{r.date}</Text>
                  {r.sub ? <Text style={{ fontSize: 11, color: "#AAA", marginTop: 1 }}>{r.sub}</Text> : null}
                </View>
                {r.type === "medication" && !r.completed && (
                  <TouchableOpacity
                    onPress={() => handleMarkMedTaken(r.id)}
                    style={{
                      backgroundColor: "#E8F5EE", borderRadius: 20,
                      paddingHorizontal: 12, paddingVertical: 6,
                    }}
                  >
                    <Text style={{ fontSize: 11, color: "#2D7A4F", fontWeight: "700" }}>Taken</Text>
                  </TouchableOpacity>
                )}
                {r.completed && (
                  <Ionicons name="checkmark-circle" size={22} color="#2D7A4F" />
                )}
                {!r.completed && r.type !== "medication" && (
                  <Ionicons name="ellipsis-horizontal" size={20} color="#CCC" />
                )}
              </TouchableOpacity>
            ))
          )}
        </View>

        {/* Add Reminder button */}
        <View style={{ paddingHorizontal: 20, marginTop: 24 }}>
          <TouchableOpacity
            onPress={() => setShowAddModal(true)}
            style={{
              backgroundColor: "#2D7A4F", borderRadius: 14,
              paddingVertical: 17, flexDirection: "row",
              alignItems: "center", justifyContent: "center", gap: 8,
              shadowColor: "#2D7A4F", shadowOffset: { width: 0, height: 6 },
              shadowOpacity: 0.3, shadowRadius: 12, elevation: 6,
            }}
          >
            <Ionicons name="add" size={20} color="#fff" />
            <Text style={{ color: "#fff", fontWeight: "700", fontSize: 16 }}>Add Reminder</Text>
          </TouchableOpacity>
        </View>

      </ScrollView>
    </SafeAreaView>
  );
}

function FormField({ placeholder, value, onChangeText, keyboardType = "default" }: any) {
  return (
    <TextInput
      placeholder={placeholder}
      placeholderTextColor="#BDBDBD"
      value={value}
      onChangeText={onChangeText}
      keyboardType={keyboardType}
      style={{
        borderWidth: 1, borderColor: "#EFEFEF", borderRadius: 12,
        backgroundColor: "#FAFAFA", paddingHorizontal: 14,
        height: 50, fontSize: 14, color: "#333",
      }}
    />
  );
}
