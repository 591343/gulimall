<template>
  <el-dialog
    :title="!dataForm.id ? '新增' : '修改'"
    :close-on-click-modal="false"
    :visible.sync="visible">
    <el-form :model="dataForm" :rules="dataRule" ref="dataForm" @keyup.enter.native="dataFormSubmit()" label-width="80px">
    <el-form-item label="???" prop="name">
      <el-input v-model="dataForm.name" placeholder="???"></el-input>
    </el-form-item>
    <el-form-item label="ͼƬ??ַ" prop="pic">
      <el-input v-model="dataForm.pic" placeholder="ͼƬ??ַ"></el-input>
    </el-form-item>
    <el-form-item label="??ʼʱ?" prop="startTime">
      <el-input v-model="dataForm.startTime" placeholder="??ʼʱ?"></el-input>
    </el-form-item>
    <el-form-item label="????ʱ?" prop="endTime">
      <el-input v-model="dataForm.endTime" placeholder="????ʱ?"></el-input>
    </el-form-item>
    <el-form-item label="״̬" prop="status">
      <el-input v-model="dataForm.status" placeholder="״̬"></el-input>
    </el-form-item>
    <el-form-item label="??????" prop="clickCount">
      <el-input v-model="dataForm.clickCount" placeholder="??????"></el-input>
    </el-form-item>
    <el-form-item label="???????????ӵ?ַ" prop="url">
      <el-input v-model="dataForm.url" placeholder="???????????ӵ?ַ"></el-input>
    </el-form-item>
    <el-form-item label="??ע" prop="note">
      <el-input v-model="dataForm.note" placeholder="??ע"></el-input>
    </el-form-item>
    <el-form-item label="???" prop="sort">
      <el-input v-model="dataForm.sort" placeholder="???"></el-input>
    </el-form-item>
    <el-form-item label="?????" prop="publisherId">
      <el-input v-model="dataForm.publisherId" placeholder="?????"></el-input>
    </el-form-item>
    <el-form-item label="?????" prop="authId">
      <el-input v-model="dataForm.authId" placeholder="?????"></el-input>
    </el-form-item>
    </el-form>
    <span slot="footer" class="dialog-footer">
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="dataFormSubmit()">确定</el-button>
    </span>
  </el-dialog>
</template>

<script>
  export default {
    data () {
      return {
        visible: false,
        dataForm: {
          id: 0,
          name: '',
          pic: '',
          startTime: '',
          endTime: '',
          status: '',
          clickCount: '',
          url: '',
          note: '',
          sort: '',
          publisherId: '',
          authId: ''
        },
        dataRule: {
          name: [
            { required: true, message: '???不能为空', trigger: 'blur' }
          ],
          pic: [
            { required: true, message: 'ͼƬ??ַ不能为空', trigger: 'blur' }
          ],
          startTime: [
            { required: true, message: '??ʼʱ?不能为空', trigger: 'blur' }
          ],
          endTime: [
            { required: true, message: '????ʱ?不能为空', trigger: 'blur' }
          ],
          status: [
            { required: true, message: '״̬不能为空', trigger: 'blur' }
          ],
          clickCount: [
            { required: true, message: '??????不能为空', trigger: 'blur' }
          ],
          url: [
            { required: true, message: '???????????ӵ?ַ不能为空', trigger: 'blur' }
          ],
          note: [
            { required: true, message: '??ע不能为空', trigger: 'blur' }
          ],
          sort: [
            { required: true, message: '???不能为空', trigger: 'blur' }
          ],
          publisherId: [
            { required: true, message: '?????不能为空', trigger: 'blur' }
          ],
          authId: [
            { required: true, message: '?????不能为空', trigger: 'blur' }
          ]
        }
      }
    },
    methods: {
      init (id) {
        this.dataForm.id = id || 0
        this.visible = true
        this.$nextTick(() => {
          this.$refs['dataForm'].resetFields()
          if (this.dataForm.id) {
            this.$http({
              url: this.$http.adornUrl(`/coupon/homeadv/info/${this.dataForm.id}`),
              method: 'get',
              params: this.$http.adornParams()
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.dataForm.name = data.homeAdv.name
                this.dataForm.pic = data.homeAdv.pic
                this.dataForm.startTime = data.homeAdv.startTime
                this.dataForm.endTime = data.homeAdv.endTime
                this.dataForm.status = data.homeAdv.status
                this.dataForm.clickCount = data.homeAdv.clickCount
                this.dataForm.url = data.homeAdv.url
                this.dataForm.note = data.homeAdv.note
                this.dataForm.sort = data.homeAdv.sort
                this.dataForm.publisherId = data.homeAdv.publisherId
                this.dataForm.authId = data.homeAdv.authId
              }
            })
          }
        })
      },
      // 表单提交
      dataFormSubmit () {
        this.$refs['dataForm'].validate((valid) => {
          if (valid) {
            this.$http({
              url: this.$http.adornUrl(`/coupon/homeadv/${!this.dataForm.id ? 'save' : 'update'}`),
              method: 'post',
              data: this.$http.adornData({
                'id': this.dataForm.id || undefined,
                'name': this.dataForm.name,
                'pic': this.dataForm.pic,
                'startTime': this.dataForm.startTime,
                'endTime': this.dataForm.endTime,
                'status': this.dataForm.status,
                'clickCount': this.dataForm.clickCount,
                'url': this.dataForm.url,
                'note': this.dataForm.note,
                'sort': this.dataForm.sort,
                'publisherId': this.dataForm.publisherId,
                'authId': this.dataForm.authId
              })
            }).then(({data}) => {
              if (data && data.code === 0) {
                this.$message({
                  message: '操作成功',
                  type: 'success',
                  duration: 1500,
                  onClose: () => {
                    this.visible = false
                    this.$emit('refreshDataList')
                  }
                })
              } else {
                this.$message.error(data.msg)
              }
            })
          }
        })
      }
    }
  }
</script>
