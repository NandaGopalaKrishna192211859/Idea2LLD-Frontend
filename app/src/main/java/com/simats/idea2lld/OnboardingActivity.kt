package com.simats.idea2lld

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.simats.idea2lld.databinding.ActivityOnboardingBinding
import android.view.View


class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var onboardingItems: List<OnboardingItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pages
        onboardingItems = listOf(
            OnboardingItem(R.drawable.page1icon, "Turn Ideas Into Architecture",
                "Transform your startup ideas into comprehensive low-level designs with AI-powered generation"),

            OnboardingItem(R.drawable.page2icon, "Smart Q&A System",
                "Answer simple questions and let AI understand your requirements"),

            OnboardingItem(R.drawable.page3icon, "Diagrams & Schemas",
                "Generate detailed architecture diagrams and schema instantly"),

            OnboardingItem(R.drawable.page4icon, "Export & Share",
                "Export as PNG, PDF, DOCX, or JSON effortlessly")
        )

        val adapter = OnboardingAdapter(onboardingItems)
        binding.onboardingViewPager.adapter = adapter

        // Indicators
        setupIndicators(onboardingItems.size)
        setCurrentIndicator(0)

        // ⭐ Single onPageChangeCallback — handles BOTH indicators + button logic
        binding.onboardingViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    setCurrentIndicator(position)

                    val lastPage = onboardingItems.size - 1

                    if (position == lastPage) {
                        // LAST PAGE → show Get Started only
                        binding.getStartedBtn.visibility = View.VISIBLE
                        binding.nextBtn.visibility = View.GONE
                        binding.skipBtn.visibility = View.GONE
                    } else {
                        // OTHER PAGES → show Next + Skip
                        binding.getStartedBtn.visibility = View.GONE
                        binding.nextBtn.visibility = View.VISIBLE
                        binding.skipBtn.visibility = View.VISIBLE
                    }
                }
            }
        )

        // NEXT button
        binding.nextBtn.setOnClickListener {
            if (binding.onboardingViewPager.currentItem < onboardingItems.size - 1) {
                binding.onboardingViewPager.currentItem += 1
            } else {
                goToLogin()
            }
        }

        // SKIP button
        binding.skipBtn.setOnClickListener {
            goToLogin()
        }

        // GET STARTED button
        binding.getStartedBtn.setOnClickListener {
            goToLogin()
        }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun setupIndicators(count: Int) {
        val indicators = arrayOfNulls<ImageView>(count)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]!!.setImageResource(R.drawable.dot_inactive)
            indicators[i]!!.layoutParams = layoutParams
            binding.onboardingDots.addView(indicators[i])
        }
    }

    private fun setCurrentIndicator(index: Int) {
        val count = binding.onboardingDots.childCount
        for (i in 0 until count) {
            val imageView = binding.onboardingDots.getChildAt(i) as ImageView
            if (i == index) {
                imageView.setImageResource(R.drawable.dot_active)
            } else {
                imageView.setImageResource(R.drawable.dot_inactive)
            }
        }
    }
}
