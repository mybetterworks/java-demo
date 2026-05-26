import React from 'react';
import ReactDOM from 'react-dom/client'; // 引入 ReactDOM 用于渲染 React 组件
import { ConfigProvider } from 'antd'; // 引入 Ant Design 的 ConfigProvider，用于全局配置
import zhCN from 'antd/locale/zh_CN'; // 引入 Ant Design 的中文语言包
import 'antd/dist/reset.css'; // 引入 Ant Design 的全局样式重置
import './styles.css'; // 引入自定义的全局样式
import { RootApp } from './App'; // 引入根组件 RootApp

// 使用 ReactDOM 创建根节点并渲染应用
ReactDOM.createRoot(document.getElementById('root')!).render(
    <React.StrictMode>
        {/* React.StrictMode 用于在开发模式下启用额外的检查 */}
        <ConfigProvider
            locale={zhCN} // 设置 Ant Design 的语言为中文
            theme={{
                // 自定义主题配置
                token: {
                    colorPrimary: '#0f766e', // 设置主色调
                    colorSuccess: '#16a34a', // 设置成功状态的颜色
                    colorWarning: '#d97706', // 设置警告状态的颜色
                    borderRadius: 12, // 设置全局圆角大小
                    fontFamily: '"HarmonyOS Sans", "Microsoft YaHei UI", "Source Han Sans SC", sans-serif' // 设置全局字体
                },
                components: {
                    // 针对特定组件的样式配置
                    Layout: {
                        headerBg: 'transparent', // 设置 Layout 组件的 Header 背景为透明
                        siderBg: 'rgba(10, 37, 64, 0.92)' // 设置 Layout 组件的 Sider 背景颜色
                    },
                    Menu: {
                        darkItemBg: 'transparent', // 设置 Menu 组件的暗色模式下菜单项背景为透明
                        darkItemSelectedBg: 'rgba(20, 184, 166, 0.24)' // 设置 Menu 组件的暗色模式下选中菜单项背景颜色
                    }
                }
            }}
        >
            {/* 渲染根组件 RootApp */}
            <RootApp />
        </ConfigProvider>
    </React.StrictMode>
);