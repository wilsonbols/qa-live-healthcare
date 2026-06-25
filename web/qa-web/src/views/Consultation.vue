<template>
  <div class="consultation">
    <div class="consultation-container">
      <!-- ====== 患者身份验证 ====== -->
      <div v-if="!currentPatient" class="auth-section">
        <div class="auth-card">
          <h1>患者身份验证</h1>
          <p>请输入您的姓名和生日以验证身份</p>
          <a-form
            :model="authForm"
            :rules="authRules"
            @finish="verifyPatient"
            layout="vertical"
          >
            <a-form-item label="姓名" name="name">
              <a-input
                v-model:value="authForm.name"
                size="large"
                placeholder="请输入您的姓名"
              >
                <template #prefix>
                  <UserOutlined />
                </template>
              </a-input>
            </a-form-item>

            <a-form-item label="生日" name="birthday">
              <a-date-picker
                v-model:value="authForm.birthday"
                size="large"
                format="YYYY-MM-DD"
                placeholder="请选择您的生日"
                style="width: 100%"
              />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" size="large" block>
                验证身份
              </a-button>
            </a-form-item>
          </a-form>

          <a-alert
            message="提示"
            description="输入任意姓名和生日即可使用。首次输入会自动创建账户,再次输入相同信息即可登录。"
            type="info"
            show-icon
          />
        </div>
      </div>

      <!-- ====== 患者门户 ====== -->
      <div v-else class="patient-portal">
        <div class="portal-header">
          <div class="patient-info">
            <UserOutlined class="patient-icon-large" />
            <div>
              <h1>{{ currentPatient.name }} 的问诊</h1>
              <p>欢迎使用在线问诊服务</p>
            </div>
          </div>
          <div class="portal-actions">
            <a-button @click="logoutPatient">
              <LogoutOutlined />
              切换用户
            </a-button>
          </div>
        </div>

        <div class="selected-doctor" v-if="selectedDoctor">
          <a-alert
            :message="`当前诊室: ${selectedDoctor.name} - ${selectedDoctor.department}`"
            type="success"
            show-icon
            closable
            @close="clearSelectedDoctor"
          />
        </div>

        <!-- ====== 我的问题列表 ====== -->
        <div class="questions-section">
          <div class="section-header">
            <h2>我的问题 ({{ myQuestions.length }})</h2>
            <a-space>
              <a-button @click="refreshQuestions" :loading="loading">
                <ReloadOutlined />
                刷新
              </a-button>
              <a-button type="primary" @click="showSubmitModal">
                <PlusOutlined />
                提交问题
              </a-button>
            </a-space>
          </div>

          <a-spin :spinning="loading">
            <!-- 筛选栏 -->
            <div class="filter-bar">
              <a-range-picker
                v-model:value="filterDateRange"
                :placeholder="['开始日期', '结束日期']"
                format="YYYY-MM-DD"
                :allowClear="true"
                style="width: 260px"
              />
              <a-select
                v-model:value="filterDoctorId"
                placeholder="全部医生"
                :allowClear="true"
                style="width: 180px"
              >
                <a-select-option value="">全部医生</a-select-option>
                <a-select-option
                  v-for="doctor in store.getActiveDoctors()"
                  :key="doctor.id"
                  :value="doctor.id"
                >
                  {{ doctor.name }}
                </a-select-option>
              </a-select>
              <a-button type="primary" @click="applyFilter">
                <SearchOutlined /> 查询
              </a-button>
              <a-button @click="resetFilter">
                <ReloadOutlined /> 重置
              </a-button>
            </div>

            <a-empty v-if="myQuestions.length === 0 && !loading" description="暂无匹配的问题" />

            <div v-else class="my-questions-list">
              <a-card
                v-for="question in myQuestions"
                :key="question.id"
                class="question-item"
              >
                <template #title>
                  <div class="question-title">
                    <span>{{ question.doctorName }}</span>
                    <a-space>
                      <a-tag :color="question.status === 'answered' ? 'green' : 'orange'">
                        {{ question.status === 'answered' ? '已解答' : '待解答' }}
                      </a-tag>
                    </a-space>
                  </div>
                </template>
                <div class="question-detail">
                  <p class="question-text"><strong>问题:</strong> {{ question.question }}</p>
                  <p class="submit-time">提交时间: {{ formatTime(question.submitTime!) }}</p>
                  <div v-if="question.status === 'answered'" class="answer-section">
                    <a-divider />
                    <p class="answer-text"><strong>医生回复:</strong> {{ question.answer }}</p>
                    <p class="answer-time">回复时间: {{ formatTime(question.answerTime!) }}</p>
                  </div>
                </div>
                <template #actions>
                  <a-popconfirm
                    title="确定要删除这个问题吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDeleteQuestion(question.id!)"
                  >
                    <a-button type="link" danger size="small">
                      <DeleteOutlined /> 删除
                    </a-button>
                  </a-popconfirm>
                  <a-button
                    type="link"
                    size="small"
                    @click="showEditModal(question)"
                    v-if="question.status === 'pending'"
                  >
                    <EditOutlined /> 编辑
                  </a-button>
                </template>
              </a-card>
            </div>
          </a-spin>
        </div>
      </div>
    </div>

    <!-- ====== 提交问题 Modal ====== -->
    <a-modal
      v-model:open="submitModalVisible"
      title="提交问题"
      @ok="submitQuestion"
      @cancel="closeSubmitModal"
      :confirmLoading="submitting"
      width="600px"
    >
      <a-form layout="vertical">
        <a-form-item label="选择医生" required>
          <a-select
            v-model:value="questionForm.doctorId"
            size="large"
            placeholder="请选择您要咨询的医生"
            :disabled="!!selectedDoctor"
          >
            <a-select-option
              v-for="doctor in availableDoctors"
              :key="doctor.id"
              :value="doctor.id"
            >
              <div class="doctor-option">
                <img :src="doctor.avatar" :alt="doctor.name" class="doctor-option-avatar" />
                <div>
                  <div>{{ doctor.name }}</div>
                  <div style="font-size: 12px; color: #999;">
                    {{ doctor.title }} · {{ doctor.department }}
                  </div>
                </div>
              </div>
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="您的问题" required>
          <a-textarea
            v-model:value="questionForm.question"
            :rows="6"
            placeholder="请详细描述您的症状或问题..."
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- ====== 编辑问题 Modal ====== -->
    <a-modal
      v-model:open="editModalVisible"
      title="编辑问题"
      @ok="handleUpdateQuestion"
      @cancel="editModalVisible = false"
      :confirmLoading="submitting"
      width="600px"
    >
      <a-form layout="vertical">
        <a-form-item label="问题内容" required>
          <a-textarea
            v-model:value="editForm.question"
            :rows="6"
            placeholder="请修改您的问题..."
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { message } from 'ant-design-vue';
import dayjs, { Dayjs } from 'dayjs';
import {
  UserOutlined,
  LogoutOutlined,
  PlusOutlined,
  ReloadOutlined,
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
} from '@ant-design/icons-vue';
import { store, Doctor } from '../store';
import {
  getQuestions,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  type QuestionDTO,
  type CreateQuestionRequest,
} from '../api';

const route = useRoute();

// ---- 患者状态 ----
const currentPatient = computed(() => store.state.currentPatient);
const loading = ref(false);
const myQuestions = ref<QuestionDTO[]>([]);

// ---- 医生选择 ----
const selectedDoctor = ref<Doctor | null>(null);

const availableDoctors = computed(() => {
  return selectedDoctor.value
    ? [selectedDoctor.value]
    : store.getActiveDoctors();
});

// ---- 身份验证 ----
const authForm = reactive({
  name: '',
  birthday: null as Dayjs | null,
});

const authRules = {
  name: [{ required: true, message: '请输入姓名' }],
  birthday: [{ required: true, message: '请选择生日' }],
};

// ---- 提交问题 ----
const submitModalVisible = ref(false);
const submitting = ref(false);

const questionForm = reactive({
  doctorId: '',
  question: '',
});

// ---- 筛选条件 ----
const filterDateRange = ref<[Dayjs, Dayjs] | null>(null);
const filterDoctorId = ref<string>('');

// ---- 编辑问题 ----
const editModalVisible = ref(false);
const editingQuestionId = ref<number>(0);
const editForm = reactive({
  question: '',
});

// ==================== 初始化 ====================
onMounted(() => {
  const doctorUsername = route.params.doctorUsername as string;
  if (doctorUsername) {
    const doctor = store.getDoctorByUsername(doctorUsername);
    if (doctor && doctor.isActive) {
      selectedDoctor.value = doctor;
    }
  }
});

// ==================== 数据加载 ====================
async function refreshQuestions() {
  if (!currentPatient.value) return;
  loading.value = true;
  try {
    const params: any = {
      patientId: parseNumericId(currentPatient.value.id),
    };
    // 附加筛选条件
    if (filterDoctorId.value) {
      params.doctorId = parseNumericId(filterDoctorId.value);
    }
    if (filterDateRange.value) {
      const [start, end] = filterDateRange.value;
      params.startDate = start.startOf('day').toISOString();
      params.endDate = end.endOf('day').toISOString();
    }
    myQuestions.value = await getQuestions(params);
  } catch (e: any) {
    message.error('加载问题失败: ' + e.message);
  } finally {
    loading.value = false;
  }
}

function applyFilter() {
  refreshQuestions();
}

function resetFilter() {
  filterDateRange.value = null;
  filterDoctorId.value = '';
  refreshQuestions();
}

// ==================== 身份验证 ====================
const verifyPatient = () => {
  const birthday = authForm.birthday?.format('YYYY-MM-DD');
  if (!birthday) {
    message.error('请选择生日');
    return;
  }

  const existingPatientCount = store.state.patients.filter(
    p => p.name === authForm.name && p.birthday === birthday
  ).length;

  store.verifyPatient(authForm.name, birthday);

  if (existingPatientCount > 0) {
    message.success('验证成功,欢迎回来!');
  } else {
    message.success('首次登录,已为您创建账户!');
  }

  // 加载该患者的问题
  refreshQuestions();
};

const logoutPatient = () => {
  store.logoutPatient();
  selectedDoctor.value = null;
  myQuestions.value = [];
  message.success('已切换用户');
};

// ==================== 医生选择 ====================
const clearSelectedDoctor = () => {
  selectedDoctor.value = null;
};

// ==================== 提交问题 ====================
const showSubmitModal = () => {
  if (selectedDoctor.value) {
    questionForm.doctorId = selectedDoctor.value.id;
  }
  questionForm.question = '';
  submitModalVisible.value = true;
};

const closeSubmitModal = () => {
  submitModalVisible.value = false;
  if (!selectedDoctor.value) {
    questionForm.doctorId = '';
  }
  questionForm.question = '';
};

const submitQuestion = async () => {
  if (!questionForm.doctorId) {
    message.error('请选择医生');
    return;
  }
  if (!questionForm.question.trim()) {
    message.error('请输入问题');
    return;
  }
  if (!currentPatient.value) return;

  submitting.value = true;
  try {
    const doctor = store.state.doctors.find(d => d.id === questionForm.doctorId);
    const payload: CreateQuestionRequest = {
      patientId: parseNumericId(currentPatient.value.id),
      patientName: currentPatient.value.name,
      doctorId: parseNumericId(questionForm.doctorId),
      doctorName: doctor?.name || '',
      question: questionForm.question.trim(),
    };
    await createQuestion(payload);
    message.success('问题提交成功');
    closeSubmitModal();
    await refreshQuestions();
  } catch (e: any) {
    message.error('提交失败: ' + e.message);
  } finally {
    submitting.value = false;
  }
};

/** 将 "doc001" 转为 1, "patient123" 转为 123, 纯数字字符串直接转 */
function parseNumericId(id: string): number {
  const match = id.match(/\d+/);
  return match ? parseInt(match[0], 10) : 0;
}

// ==================== 编辑问题 ====================
const showEditModal = (question: QuestionDTO) => {
  editingQuestionId.value = question.id!;
  editForm.question = question.question;
  editModalVisible.value = true;
};

const handleUpdateQuestion = async () => {
  if (!editForm.question.trim()) {
    message.error('问题内容不能为空');
    return;
  }
  if (!currentPatient.value) return;

  submitting.value = true;
  try {
    await updateQuestion(editingQuestionId.value, {
      patientId: parseNumericId(currentPatient.value.id),
      patientName: currentPatient.value.name,
      doctorId: 0,
      question: editForm.question.trim(),
    });
    message.success('问题更新成功');
    editModalVisible.value = false;
    await refreshQuestions();
  } catch (e: any) {
    message.error('更新失败: ' + e.message);
  } finally {
    submitting.value = false;
  }
};

// ==================== 删除问题 ====================
const handleDeleteQuestion = async (id: number) => {
  try {
    await deleteQuestion(id);
    message.success('问题已删除');
    await refreshQuestions();
  } catch (e: any) {
    message.error('删除失败: ' + e.message);
  }
};

// ==================== 工具 ====================
const formatTime = (time: string) => {
  return dayjs(time).format('YYYY-MM-DD HH:mm');
};
</script>

<style scoped>
.consultation {
  min-height: calc(100vh - 64px);
  padding-top: 64px;
  background: #f0f2f5;
}

.consultation-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.auth-section {
  min-height: calc(100vh - 112px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-card {
  background: #fff;
  border-radius: 16px;
  padding: 48px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.auth-card h1 {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  text-align: center;
  margin-bottom: 8px;
}

.auth-card > p {
  font-size: 16px;
  color: #666;
  text-align: center;
  margin-bottom: 32px;
}

.patient-portal {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.portal-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
}

.patient-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.patient-icon-large {
  font-size: 48px;
  color: #fff;
}

.patient-info h1 {
  font-size: 24px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 4px;
}

.patient-info p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
}

.selected-doctor {
  padding: 16px 24px;
  background: #f6ffed;
  border-bottom: 1px solid #e8e8e8;
}

.questions-section {
  padding: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  flex-wrap: wrap;
}

.my-questions-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-item {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.question-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.question-detail {
  line-height: 1.6;
}

.question-text,
.answer-text {
  margin-bottom: 12px;
  color: #333;
}

.submit-time,
.answer-time {
  font-size: 12px;
  color: #999;
  margin: 0;
}

.answer-section {
  margin-top: 16px;
}

.doctor-option {
  display: flex;
  align-items: center;
  gap: 12px;
}

.doctor-option-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

@media (max-width: 768px) {
  .auth-card {
    margin: 24px;
    padding: 32px 24px;
  }

  .portal-header {
    flex-direction: column;
    gap: 16px;
    align-items: flex-start;
  }

  .portal-actions {
    width: 100%;
  }
}
</style>
