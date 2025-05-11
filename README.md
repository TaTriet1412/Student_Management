Tài khoản Google để đăng nhập Firebase console, Github và Render dashboard
	Username: innovatechtdtu@gmail.com
	password: InnovaTechTdtu@123
	
Link đến Console: https://console.firebase.google.com/u/0/project/quanlyhocvien-innovatech/overview
Link đến Render dashboard: https://dashboard.render.com/  (hỗ trợ chạy local client - có thể không cần setup nếu chỉ dùng firestore)


Code và hướng dẫn triển khai API: (hỗ trợ chạy local client - có thể không cần setup nếu chỉ dùng firestore)
     FastAPI
	- Đây là API để ứng dụng có thể dùng Firebase admin SDK thực hiện các chức năng như Cấp tài khoản, Lấy danh sách nhân viên,...
	- API này là bắt buộc để ứng dụng có thể hoạt động.
	- API đã được triển khai và hoạt động tại Render.com, ứng dụng đã được thiết lập sẵn để kết nối với API.
        - Hướng dẫn này dành cho trường hợp cần triển khai API ở một máy chủ mới.
     Triển khai API ở local
	1/ Cài đặt FastAPI, Uvicorn (server để chạy FastAPI), Firebase Admin SDK và Thư viện đọc biến môi trường
		pip install fastapi uvicorn firebase-admin python-dotenv
	2/ Chạy API
		- Yêu cầu python 3.8 trở lên
		- Mở powershell trong thư mục API_Local
		- Chạy API: 
			uvicorn main:app --reload
 	3/ Kiểm tra: mở PowerShell và chạy lệnh
		Invoke-WebRequest -Uri "http://127.0.0.1:8000/connect_check/" -Method GET -Headers @{ "x-api-key" = "05badc8fe5c64c0d96c2ec54d970d2dc" }
		Dừng API: Ctrl + c
	4/ Cấu hình lại project Android:
		Sửa biến BASE_API_URL ở CsvDownloader.java thành “http://127.0.0.1:8000/”
		Sửa biến BASE_URL ở APIClient.java thành “http://127.0.0.1:8000/”
     Triển khai API trên Render.com
	1/ Tạo dự án mới ở Github/Gitlab. Push tất cả các tệp ở thư mục API_Render lên Github/Gitlab
	2/ Tạo tài khoản Render.com
	3/ Truy cập https://dashboard.render.com/ > new Web Service > deloy API theo hướng dẫn ở trang web 
		- Đặt tên dự án mới là Android Midterm essay API
		- ở mục Start Command điền uvicorn main:app --host 0.0.0.0 --port $PORT
	4/ Thêm Firebase Admin SDK và API key: 
		- Truy cập Dashboard > Project > Android Midterm essay API  >Environment Variables  > Secret Files > add from .env > tải lên tệp secret_key.env
	5/ Kiểm tra: mở PowerShell và chạy lệnh
		Invoke-WebRequest -Uri "https://android-midterm-essay-api.onrender.com/connect_check/" -Method GET -Headers @{ "x-api-key" = "05badc8fe5c64c0d96c2ec54d970d2dc" }

Các tài khoản phục vụ demo của ứng dụng:
	Admin: innovatechtdtu@gmail.com / 123456
	Quản lý: tatriet16@gmail.com / 123456
	Nhân viên: thanquocthinh@gmail.com / 123456
	
APK setting:  (hỗ trợ chạy local client - có thể không cần setup nếu chỉ dùng firestore)
	Key Store Password: 123456
	Key Alias: quanlisinhvien
	Key password: 123456
	
File Video demo:
	https://drive.google.com/file/d/1J0aNRkbqLfGGZr-OifTViav5WPlxG7H0/view?usp=sharing

Setting:
	MinSDK: API 29 (Android 10)
